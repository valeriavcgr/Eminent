package com.example.Eminent.participacion.repository;

import com.example.Eminent.participacion.entity.Inscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Inscripcion}.
 * Proporciona operaciones CRUD estándar más consultas personalizadas
 * para búsquedas por participante, evento y estado, incluyendo paginación.
 */
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    /** Busca una inscripción específica por participante y evento. */
    Optional<Inscripcion> findByParticipanteIdAndEventoId(Long participanteId, Long eventoId);

    /** Lista paginada de inscripciones de un evento filtradas por estado. */
    Page<Inscripcion> findByEventoIdAndEstado(Long eventoId, Inscripcion.Estado estado, Pageable pageable);

    /** Cuenta las inscripciones de un evento con el estado dado. */
    long countByEventoIdAndEstado(Long eventoId, Inscripcion.Estado estado);

    /** Lista todas las inscripciones de un evento con estado dado, ordenadas por fecha de inscripción ascendente. */
    List<Inscripcion> findByEventoIdAndEstadoOrderByFechaInscripcionAsc(Long eventoId, Inscripcion.Estado estado);

    /** Lista paginada de todas las inscripciones de un evento sin importar el estado. */
    Page<Inscripcion> findByEventoId(Long eventoId, Pageable pageable);

    /** Lista paginada de inscripciones de un participante, ordenadas por fecha descendente. */
    Page<Inscripcion> findByParticipanteIdOrderByFechaInscripcionDesc(Long participanteId, Pageable pageable);
}