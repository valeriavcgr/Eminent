package com.example.Eminent.eventos.repository;

import com.example.Eminent.eventos.entity.EventoDia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventoDiaRepository extends JpaRepository<EventoDia, Long> {

    /** Lista las jornadas de un evento ordenadas por número de día. */
    List<EventoDia> findByEvento_IdOrderByNumeroDiaAsc(Long eventoId);

    /** Lista las jornadas de un evento ordenadas por fecha (usado al renumerar tras editar). */
    List<EventoDia> findByEvento_IdOrderByFechaAsc(Long eventoId);

    /** Cuenta el número de jornadas de un evento. */
    long countByEvento_Id(Long eventoId);

    /** Verifica si ya existe una jornada para esa fecha dentro del evento. */
    boolean existsByEvento_IdAndFecha(Long eventoId, LocalDate fecha);
}
