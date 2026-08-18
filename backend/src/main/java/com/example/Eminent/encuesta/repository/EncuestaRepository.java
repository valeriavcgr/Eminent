package com.example.Eminent.encuesta.repository;

import com.example.Eminent.encuesta.entity.Encuesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EncuestaRepository extends JpaRepository<Encuesta, Long> {

    boolean existsByInscripcionId(Long inscripcionId);

    @Query("SELECT e FROM Encuesta e WHERE e.inscripcion.evento.id = :eventoId " +
            "AND (:calificacion IS NULL OR e.calificacion = :calificacion) " +
            "ORDER BY e.fechaCreacion DESC")
    Page<Encuesta> buscarPorEvento(@Param("eventoId") Long eventoId,
                                    @Param("calificacion") Integer calificacion,
                                    Pageable pageable);

    @Query("SELECT AVG(e.calificacion) FROM Encuesta e WHERE e.inscripcion.evento.id = :eventoId")
    Double promedioPorEvento(@Param("eventoId") Long eventoId);

    @Query("SELECT AVG(e.calificacion) FROM Encuesta e")
    Double promedioGeneral();
}