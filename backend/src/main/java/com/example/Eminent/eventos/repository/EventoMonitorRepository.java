package com.example.Eminent.eventos.repository;

import com.example.Eminent.eventos.entity.EventoMonitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link EventoMonitor}.
 * Proporciona operaciones CRUD estándar más consultas para encontrar asignaciones
 * de monitores por monitor o por evento, y verificar la existencia de una asignación.
 */
public interface EventoMonitorRepository extends JpaRepository<EventoMonitor, Long> {

    /** Lista todas las asignaciones de monitores para el monitor con el ID proporcionado. */
    List<EventoMonitor> findByMonitor_Id(Long monitorId);

    /** Lista todas las asignaciones de monitores para el evento con el ID proporcionado. */
    List<EventoMonitor> findByEvento_Id(Long eventoId);

    /** Verifica si ya existe una asignación de monitor para el evento y monitor dados. */
    boolean existsByEvento_IdAndMonitor_Id(Long eventoId, Long monitorId);
}