package com.example.Eminent.dashboard.service;

import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.dashboard.dto.DashboardDTO;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired private EventoRepository eventoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private AsistenciaRepository asistenciaRepository;

    public DashboardDTO obtenerResumen(Evento.Tipo tipo, Evento.Modalidad modalidad, Evento.Estado estado,
                                       LocalDateTime fechaDesde, LocalDateTime fechaHasta, Usuario usuario) {

        LocalDateTime desde = (fechaDesde != null) ? fechaDesde : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime hasta = (fechaHasta != null) ? fechaHasta : LocalDateTime.of(2100, 1, 1, 0, 0);

        List<Evento> eventos = eventoRepository.buscarConFiltros(tipo, modalidad, estado, desde, hasta);

        // Si es Operador, solo ve los eventos que él creó (Admin puede ve todos)
        if (usuario.getRol() == Usuario.Rol.OPERADOR) {
            eventos = eventos.stream()
                    .filter(e -> e.getCreadoPor().getId().equals(usuario.getId()))
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

        for (Evento evento : eventos) {
            aforoTotal += evento.getAforo();
            inscritosActivosTotal += inscripcionRepository.countByEventoIdAndEstado(evento.getId(), Inscripcion.Estado.ACTIVA);
            asistieronTotal += asistenciaRepository.countByInscripcion_Evento_Id(evento.getId());
        }

        dto.setAforoTotal(aforoTotal);
        dto.setInscritosActivosTotal(inscritosActivosTotal);
        dto.setAsistieronTotal(asistieronTotal);
        dto.setPorcentajeAforoOcupado(aforoTotal > 0 ? (inscritosActivosTotal * 100.0 / aforoTotal) : 0);
        dto.setPorcentajeAsistenciaSobreInscritos(inscritosActivosTotal > 0 ? (asistieronTotal * 100.0 / inscritosActivosTotal) : 0);

        return dto;
    }
}