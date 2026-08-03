package com.example.Eminent.asistencia.repository;

import com.example.Eminent.asistencia.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    /** Busca un registro de asistencia por el ID de su inscripción vinculada. */
    Optional<Asistencia> findByInscripcionId(Long inscripcionId);

    /** Verifica si existe un registro de asistencia para la inscripción dada. */
    boolean existsByInscripcionId(Long inscripcionId);

    /** Lista todos los registros de asistencia de un evento por ID del evento. */
    List<Asistencia> findByInscripcion_Evento_Id(Long eventoId);

    /** Busca un registro de asistencia por participante y evento. */
    Optional<Asistencia> findByInscripcion_Participante_IdAndInscripcion_Evento_Id(Long participanteId, Long eventoId);

    /** Cuenta el número de registros de asistencia de un evento. */
    long countByInscripcion_Evento_Id(Long eventoId);

    /** Cuenta el número de registros de asistencia de un evento por método (QR o MANUAL). */
    long countByInscripcion_Evento_IdAndMetodo(Long eventoId, Asistencia.Metodo metodo);
}