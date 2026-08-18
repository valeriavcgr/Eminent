package com.example.Eminent.eventos.service;

import com.example.Eminent.eventos.entity.EventoDia;
import com.example.Eminent.eventos.repository.EventoDiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventoDiaService {

    @Autowired
    private EventoDiaRepository eventoDiaRepository;

    public List<EventoDia> listarPorEvento(Long eventoId) {
        return eventoDiaRepository.findByEvento_IdOrderByNumeroDiaAsc(eventoId);
    }

    /** Devuelve la jornada del evento cuyo horario contiene el instante actual. */
    public Optional<EventoDia> obtenerJornadaActivaOpcional(Long eventoId) {
        LocalDateTime ahora = LocalDateTime.now();
        return eventoDiaRepository.findByEvento_IdOrderByNumeroDiaAsc(eventoId).stream()
                .filter(j -> !ahora.isBefore(LocalDateTime.of(j.getFecha(), j.getHoraInicio()))
                        && !ahora.isAfter(LocalDateTime.of(j.getFecha(), j.getHoraFin())))
                .findFirst();
    }

    /** Igual que {@link #obtenerJornadaActivaOpcional} pero lanza si no hay ninguna jornada activa ahora. */
    public EventoDia obtenerJornadaActiva(Long eventoId) {
        return obtenerJornadaActivaOpcional(eventoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay ninguna jornada activa en este momento. Solo puedes registrar asistencia dentro del horario de una jornada del evento."));
    }
}
