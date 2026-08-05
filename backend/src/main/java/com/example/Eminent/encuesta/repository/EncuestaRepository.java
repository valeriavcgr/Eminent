package com.example.Eminent.encuesta.repository;

import com.example.Eminent.encuesta.entity.Encuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EncuestaRepository extends JpaRepository<Encuesta, Long> {

    boolean existsByAsistenciaId(Long asistenciaId);

    List<Encuesta> findByAsistencia_Inscripcion_Evento_Id(Long eventoId);

    @Query("SELECT AVG(e.calificacion) FROM Encuesta e WHERE e.asistencia.inscripcion.evento.id = :eventoId")
    Double promedioPorEvento(@Param("eventoId") Long eventoId);

    @Query("SELECT AVG(e.calificacion) FROM Encuesta e")
    Double promedioGeneral();
}