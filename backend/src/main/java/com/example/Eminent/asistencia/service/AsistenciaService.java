package com.example.Eminent.asistencia.service;

import com.example.Eminent.asistencia.dto.*;
import com.example.Eminent.asistencia.entity.Asistencia;
import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.eventos.dto.EventoDiaDTO;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.EventoDia;
import com.example.Eminent.eventos.repository.EventoDiaRepository;
import com.example.Eminent.eventos.repository.EventoMonitorRepository;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.eventos.service.EventoDiaService;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    @Autowired private AsistenciaRepository asistenciaRepo;
    @Autowired private InscripcionRepository inscripcionRepo;
    @Autowired private EventoRepository eventoRepo;
    @Autowired private EventoDiaRepository eventoDiaRepo;
    @Autowired private EventoDiaService eventoDiaService;
    @Autowired private EventoMonitorRepository eventoMonitorRepo;
    @Autowired private AuditoriaService auditoriaService;

    private static final Pattern PATRON_QR = Pattern.compile("^INSCRIPCION-(\\d+)-.*$");

    public ListadoAsistenciaDTO listarParticipantes(Long eventoId, Usuario usuario, Usuario.Rol rolActivo) {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (rolActivo == Usuario.Rol.MONITOR) {
            validarMonitorAsignado(eventoId, usuario.getId());
        }

        int totalJornadas = (int) eventoDiaRepo.countByEvento_Id(eventoId);
        Optional<EventoDia> jornadaActivaOpt = eventoDiaService.obtenerJornadaActivaOpcional(eventoId);

        List<Inscripcion> activas = inscripcionRepo
                .findByEventoIdAndEstado(eventoId, Inscripcion.Estado.ACTIVA, Pageable.unpaged())
                .getContent();

        Map<Long, List<Asistencia>> asistenciasPorInscripcion = asistenciaRepo.findByInscripcion_Evento_Id(eventoId)
                .stream().collect(Collectors.groupingBy(a -> a.getInscripcion().getId()));

        List<ParticipanteAsistenciaDTO> lista = activas.stream().map(i -> {
            ParticipanteAsistenciaDTO dto = new ParticipanteAsistenciaDTO();
            dto.setInscripcionId(i.getId());
            dto.setParticipanteNombre(i.getParticipante().getNombre() + " " + i.getParticipante().getApellido());
            dto.setParticipanteDocumento(i.getParticipante().getDocumento());
            dto.setParticipanteCorreo(i.getParticipante().getCorreo());
            dto.setParticipanteTelefono(i.getParticipante().getTelefono());

            List<Asistencia> mias = asistenciasPorInscripcion.getOrDefault(i.getId(), List.of());
            dto.setDiasAsistidos(mias.size());
            dto.setAsistioCompleto(totalJornadas > 0 && mias.size() == totalJornadas);
            dto.setAsistioJornadaActiva(jornadaActivaOpt.isPresent() && mias.stream()
                    .anyMatch(a -> a.getEventoDia().getId().equals(jornadaActivaOpt.get().getId())));

            return dto;
        }).toList();

        long totalInscritos = lista.size();
        long totalAsistieron = lista.stream().filter(p -> p.getDiasAsistidos() > 0).count();
        double porcentaje = evento.getAforo() > 0 ? (totalAsistieron * 100.0 / evento.getAforo()) : 0;

        ResumenAsistenciaDTO resumen = new ResumenAsistenciaDTO();
        resumen.setTotalInscritos(totalInscritos);
        resumen.setTotalAsistieron(totalAsistieron);
        resumen.setPorcentajeAforoOcupado(porcentaje);

        ListadoAsistenciaDTO resultado = new ListadoAsistenciaDTO();
        resultado.setResumen(resumen);
        resultado.setParticipantes(lista);
        resultado.setEventoNombre(evento.getNombre());
        resultado.setEventoEstado(evento.getEstado().name());
        resultado.setTotalJornadas(totalJornadas);
        resultado.setJornadaActiva(jornadaActivaOpt.map(this::toJornadaDTO).orElse(null));
        return resultado;
    }

    public Asistencia registrarManual(Long inscripcionId, Usuario monitor) {
        Inscripcion inscripcion = inscripcionRepo.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        Long eventoId = inscripcion.getEvento().getId();
        validarMonitorAsignado(eventoId, monitor.getId());
        validarEventoEnCurso(inscripcion.getEvento());
        EventoDia jornada = eventoDiaService.obtenerJornadaActiva(eventoId);

        if (asistenciaRepo.existsByInscripcion_IdAndEventoDia_Id(inscripcionId, jornada.getId())) {
            throw new IllegalArgumentException("Este participante ya registró asistencia en la jornada de hoy");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setInscripcion(inscripcion);
        asistencia.setEventoDia(jornada);
        asistencia.setMetodo(Asistencia.Metodo.MANUAL);
        asistencia.setRegistradoPor(monitor);
        Asistencia guardada = asistenciaRepo.save(asistencia);

        auditoriaService.registrar(monitor, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.ASISTENCIA,
                guardada.getId(), "Registro manual de asistencia para inscripción " + inscripcionId
                        + " (jornada " + jornada.getNumeroDia() + ")", null);

        return guardada;
    }

    public Asistencia registrarPorQr(String contenidoQr, Long eventoId, Usuario monitor) {
        Long inscripcionId = extraerInscripcionId(contenidoQr);

        Inscripcion inscripcion = inscripcionRepo.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Código QR no válido"));

        if (!inscripcion.getContenidoQr().equals(contenidoQr)) {
            throw new IllegalArgumentException("Código QR no válido");
        }

        if (!inscripcion.getEvento().getId().equals(eventoId)) {
            throw new IllegalArgumentException("Este código no corresponde a este evento");
        }

        validarMonitorAsignado(eventoId, monitor.getId());
        validarEventoEnCurso(inscripcion.getEvento());
        EventoDia jornada = eventoDiaService.obtenerJornadaActiva(eventoId);

        if (asistenciaRepo.existsByInscripcion_IdAndEventoDia_Id(inscripcionId, jornada.getId())) {
            throw new IllegalArgumentException("Este participante ya registró asistencia en la jornada de hoy");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setInscripcion(inscripcion);
        asistencia.setEventoDia(jornada);
        asistencia.setMetodo(Asistencia.Metodo.QR);
        asistencia.setRegistradoPor(monitor);
        Asistencia guardada = asistenciaRepo.save(asistencia);

        auditoriaService.registrar(monitor, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.ASISTENCIA,
                guardada.getId(), "Registro de asistencia por QR para inscripción " + inscripcionId
                        + " (jornada " + jornada.getNumeroDia() + ")", null);

        return guardada;
    }

    private Long extraerInscripcionId(String contenidoQr) {
        Matcher m = PATRON_QR.matcher(contenidoQr);
        if (!m.matches()) {
            throw new IllegalArgumentException("Código QR no válido");
        }
        return Long.valueOf(m.group(1));
    }

    private void validarMonitorAsignado(Long eventoId, Long monitorId) {
        if (!eventoMonitorRepo.existsByEvento_IdAndMonitor_Id(eventoId, monitorId)) {
            throw new IllegalArgumentException("No estás asignado a este evento");
        }
    }

    private void validarEventoEnCurso(Evento evento) {
        if (evento.getEstado() != Evento.Estado.EN_CURSO) {
            throw new IllegalArgumentException("Solo se puede registrar asistencia mientras el evento está en curso");
        }
    }

    private EventoDiaDTO toJornadaDTO(EventoDia jornada) {
        return new EventoDiaDTO(jornada.getId(), jornada.getNumeroDia(), jornada.getFecha(),
                jornada.getHoraInicio(), jornada.getHoraFin());
    }
}
