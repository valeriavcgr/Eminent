package com.example.Eminent.participacion.repository;

import com.example.Eminent.participacion.entity.Inscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByParticipanteIdAndEventoId(Long participanteId, Long eventoId);

    Page<Inscripcion> findByEventoIdAndEstado(Long eventoId, Inscripcion.Estado estado, Pageable pageable);

    long countByEventoIdAndEstado(Long eventoId, Inscripcion.Estado estado);

    List<Inscripcion> findByEventoIdAndEstadoOrderByFechaInscripcionAsc(Long eventoId, Inscripcion.Estado estado);

    Page<Inscripcion> findByEventoId(Long eventoId, Pageable pageable);

    Page<Inscripcion> findByParticipanteIdOrderByFechaInscripcionDesc(Long participanteId, Pageable pageable);
}