package com.example.Eminent.dashboard.service;

import com.example.Eminent.asistencia.entity.Asistencia;
import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.dashboard.dto.DashboardDTO;
import com.example.Eminent.dashboard.dto.EventoResumenDashboardDTO;
import com.example.Eminent.encuesta.repository.EncuestaRepository;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.repository.EventoMonitorRepository;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired private EventoRepository eventoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private AsistenciaRepository asistenciaRepository;
    @Autowired private EventoMonitorRepository eventoMonitorRepository;
    @Autowired private EncuestaRepository encuestaRepository;

    public DashboardDTO obtenerResumen(Evento.Tipo tipo, Evento.Modalidad modalidad, Evento.Estado estado,
                                       LocalDateTime fechaDesde, LocalDateTime fechaHasta, Usuario usuario) {

        LocalDateTime desde = (fechaDesde != null) ? fechaDesde : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime hasta = (fechaHasta != null) ? fechaHasta : LocalDateTime.of(2100, 1, 1, 0, 0);

        List<Evento> eventos = eventoRepository.buscarConFiltros(tipo, modalidad, estado, desde, hasta, Pageable.unpaged()).getContent();

        if (usuario.getRol() == Usuario.Rol.OPERADOR) {
            eventos = eventos.stream()
                    .filter(e -> e.getCreadoPor().getId().equals(usuario.getId()))
                    .toList();
        } else if (usuario.getRol() == Usuario.Rol.MONITOR) {
            List<Long> idsAsignados = eventoMonitorRepository.findByMonitor_Id(usuario.getId()).stream()
                    .map(em -> em.getEvento().getId())
                    .toList();
            eventos = eventos.stream()
                    .filter(e -> idsAsignados.contains(e.getId()))
                    .toList();
        }

        DashboardDTO dto = new DashboardDTO();
        dto.setTotalEventos(eventos.size());

        dto.setEventosPorTipo(eventos.stream()
                .collect(Collectors.groupingBy(e -> e.getTipo().name(), Collectors.counting())));
        dto.setEventosPorModalidad(eventos.stream()
                .collect(Collectors.groupingBy(e -> e.getModalidad().name(), Collectors.counting())));
        dto.setEventosPorEstado(eventos.stream()
                .collect(Collectors.groupingBy(e -> e.getEstado().name(), Collectors.counting())));

        int aforoTotal = 0;
        long inscritosActivosTotal = 0;
        long asistieronTotal = 0;
        long asistieronQrTotal = 0;
        long asistieronManualTotal = 0;
        List<EventoResumenDashboardDTO> detalle = new ArrayList<>();

        for (Evento evento : eventos) {
            long inscritosEvento = inscripcionRepository.countByEventoIdAndEstado(evento.getId(), Inscripcion.Estado.ACTIVA);
            long asistieronEvento = asistenciaRepository.countByInscripcion_Evento_Id(evento.getId());

            aforoTotal += evento.getAforo();
            inscritosActivosTotal += inscritosEvento;
            asistieronTotal += asistieronEvento;
            asistieronQrTotal += asistenciaRepository.countByInscripcion_Evento_IdAndMetodo(evento.getId(), Asistencia.Metodo.QR);
            asistieronManualTotal += asistenciaRepository.countByInscripcion_Evento_IdAndMetodo(evento.getId(), Asistencia.Metodo.MANUAL);

            EventoResumenDashboardDTO resumen = new EventoResumenDashboardDTO();
            resumen.setId(evento.getId());
            resumen.setNombre(evento.getNombre());
            resumen.setTipo(evento.getTipo().name());
            resumen.setModalidad(evento.getModalidad().name());
            resumen.setAforo(evento.getAforo());
            resumen.setInscritos(inscritosEvento);
            resumen.setAsistieron(asistieronEvento);
            resumen.setPorcentajeAsistencia(inscritosEvento > 0 ? (asistieronEvento * 100.0 / inscritosEvento) : 0);
            resumen.setPromedioSatisfaccion(encuestaRepository.promedioPorEvento(evento.getId()));
            detalle.add(resumen);
        }

        detalle.sort(Comparator.comparingDouble(EventoResumenDashboardDTO::getPorcentajeAsistencia).reversed());

        dto.setAforoTotal(aforoTotal);
        dto.setInscritosActivosTotal(inscritosActivosTotal);
        dto.setAsistieronTotal(asistieronTotal);
        dto.setAsistieronQrTotal(asistieronQrTotal);
        dto.setAsistieronManualTotal(asistieronManualTotal);
        dto.setPorcentajeAforoOcupado(aforoTotal > 0 ? (inscritosActivosTotal * 100.0 / aforoTotal) : 0);
        dto.setPorcentajeAsistenciaSobreInscritos(inscritosActivosTotal > 0 ? (asistieronTotal * 100.0 / inscritosActivosTotal) : 0);
        dto.setDetalleEventos(detalle);
        dto.setPromedioSatisfaccionGeneral(encuestaRepository.promedioGeneral());

        return dto;
    }
}