package com.example.Eminent.asistencia.repository;

import com.example.Eminent.asistencia.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    Optional<Asistencia> findByInscripcionId(Long inscripcionId);
    boolean existsByInscripcionId(Long inscripcionId);

    List<Asistencia> findByInscripcion_Evento_Id(Long eventoId);

    Optional<Asistencia> findByInscripcion_Participante_IdAndInscripcion_Evento_Id(Long participanteId, Long eventoId);

    long countByInscripcion_Evento_Id(Long eventoId);
}
