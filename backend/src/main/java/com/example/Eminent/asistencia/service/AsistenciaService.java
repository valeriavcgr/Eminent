package com.example.Eminent.asistencia.service;

import com.example.Eminent.asistencia.dto.*;
import com.example.Eminent.asistencia.entity.Asistencia;
import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.repository.EventoMonitorRepository;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio para el registro de asistencia de participantes a eventos.
 * Permite listar participantes de un evento y registrar asistencia
 * tanto de forma manual (por ID de inscripción) como mediante escaneo
 * de códigos QR generados al momento de la inscripción.
 */
@Service
public class AsistenciaService {

    @Autowired private AsistenciaRepository asistenciaRepo;
    @Autowired private InscripcionRepository inscripcionRepo;
    @Autowired private EventoRepository eventoRepo;
    @Autowired private EventoMonitorRepository eventoMonitorRepo;
    @Autowired private AuditoriaService auditoriaService;

    private static final Pattern PATRON_QR = Pattern.compile("^INSCRIPCION-(\\d+)-.*$");

    public ListadoAsistenciaDTO listarParticipantes(Long eventoId, Usuario usuario) {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (usuario.getRol() == Usuario.Rol.MONITOR) {
            validarMonitorAsignado(eventoId, usuario.getId());
        }

        List<Inscripcion> activas = inscripcionRepo
                .findByEventoIdAndEstado(eventoId, Inscripcion.Estado.ACTIVA, Pageable.unpaged())
                .getContent();

        List<ParticipanteAsistenciaDTO> lista = activas.stream().map(i -> {
            ParticipanteAsistenciaDTO dto = new ParticipanteAsistenciaDTO();
            dto.setInscripcionId(i.getId());
            dto.setParticipanteNombre(i.getParticipante().getNombre() + " " + i.getParticipante().getApellido());
            dto.setParticipanteDocumento(i.getParticipante().getDocumento());
            dto.setParticipanteCorreo(i.getParticipante().getCorreo());
            dto.setParticipanteTelefono(i.getParticipante().getTelefono());

            asistenciaRepo.findByInscripcionId(i.getId()).ifPresentOrElse(a -> {
                dto.setAsistio(true);
                dto.setFechaHoraAsistencia(a.getFechaHora());
                dto.setMetodo(a.getMetodo().name());
            }, () -> dto.setAsistio(false));

            return dto;
        }).toList();

        long totalInscritos = lista.size();
        long totalAsistieron = lista.stream().filter(ParticipanteAsistenciaDTO::isAsistio).count();
        double porcentaje = evento.getAforo() > 0 ? (totalAsistieron * 100.0 / evento.getAforo()) : 0;

        ResumenAsistenciaDTO resumen = new ResumenAsistenciaDTO();
        resumen.setTotalInscritos(totalInscritos);
        resumen.setTotalAsistieron(totalAsistieron);
        resumen.setPorcentajeAforoOcupado(porcentaje);

        ListadoAsistenciaDTO resultado = new ListadoAsistenciaDTO();
        resultado.setResumen(resumen);
        resultado.setParticipantes(lista);
        resultado.setEventoEstado(evento.getEstado().name());
        return resultado;
    }

    public Asistencia registrarManual(Long inscripcionId, Usuario monitor) {
        Inscripcion inscripcion = inscripcionRepo.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        validarMonitorAsignado(inscripcion.getEvento().getId(), monitor.getId());
        validarEventoEnCurso(inscripcion.getEvento());

        if (asistenciaRepo.existsByInscripcionId(inscripcionId)) {
            throw new IllegalArgumentException("Este participante ya registró asistencia");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setInscripcion(inscripcion);
        asistencia.setMetodo(Asistencia.Metodo.MANUAL);
        asistencia.setRegistradoPor(monitor);
        Asistencia guardada = asistenciaRepo.save(asistencia);

        auditoriaService.registrar(monitor, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.ASISTENCIA,
                guardada.getId(), "Registro manual de asistencia para inscripción " + inscripcionId, null);

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

        if (asistenciaRepo.existsByInscripcionId(inscripcionId)) {
            throw new IllegalArgumentException("Este participante ya registró asistencia");
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setInscripcion(inscripcion);
        asistencia.setMetodo(Asistencia.Metodo.QR);
        asistencia.setRegistradoPor(monitor);
        Asistencia guardada = asistenciaRepo.save(asistencia);

        auditoriaService.registrar(monitor, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.ASISTENCIA,
                guardada.getId(), "Registro de asistencia por QR para inscripción " + inscripcionId, null);

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
}