package com.example.Eminent.asistencia.repository;

import com.example.Eminent.asistencia.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    /** Verifica si existe un registro de asistencia de la inscripción para esa jornada. */
    boolean existsByInscripcion_IdAndEventoDia_Id(Long inscripcionId, Long eventoDiaId);

    /** Verifica si una jornada ya tiene al menos una asistencia registrada. */
    boolean existsByEventoDia_Id(Long eventoDiaId);

    /** Cuenta cuántas jornadas distintas asistió una inscripción. */
    long countByInscripcion_Id(Long inscripcionId);

    /** Lista todas las asistencias (todas las jornadas) de una inscripción. */
    List<Asistencia> findByInscripcion_Id(Long inscripcionId);

    /** Lista todos los registros de asistencia de un evento por ID del evento. */
    List<Asistencia> findByInscripcion_Evento_Id(Long eventoId);

    /**
     * Cuenta el número de registros de asistencia de un evento (una fila por jornada asistida).
     * En eventos de varios días esto puede superar el número de inscritos: úsese solo para
     * desgloses por método (QR/MANUAL), nunca como "cuántas personas asistieron".
     */
    long countByInscripcion_Evento_Id(Long eventoId);

    /** Cuenta el número de registros de asistencia de un evento por método (QR o MANUAL). */
    long countByInscripcion_Evento_IdAndMetodo(Long eventoId, Asistencia.Metodo metodo);

    /** Cuenta cuántos participantes distintos (inscripciones) registraron al menos una asistencia en el evento. */
    @Query("SELECT COUNT(DISTINCT a.inscripcion.id) FROM Asistencia a WHERE a.inscripcion.evento.id = :eventoId")
    long countParticipantesConAsistencia(@Param("eventoId") Long eventoId);
}