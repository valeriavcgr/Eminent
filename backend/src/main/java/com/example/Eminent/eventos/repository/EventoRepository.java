package com.example.Eminent.eventos.repository;

import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.Evento.Estado;
import com.example.Eminent.eventos.entity.Evento.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    Optional<Evento> findByCreadoPor_Id(Long creadoPorId);

    List<Evento> findByEstado(Estado estado);

    List<Evento> findByEstadoIn(List<Estado> estados);

    boolean existsByNombreAndTipo(String nombre, Tipo tipo);

    List<Evento> findByEstadoAndFechaInicioLessThanEqual(Estado estado, LocalDateTime fecha);

    List<Evento> findByEstadoAndFechaFinLessThanEqual(Estado estado, LocalDateTime fecha);

    @Query("SELECT e FROM Evento e WHERE " +
            "(:tipo IS NULL OR e.tipo = :tipo) AND " +
            "(:modalidad IS NULL OR e.modalidad = :modalidad) AND " +
            "(:estado IS NULL OR e.estado = :estado) AND " +
            "e.fechaInicio >= :fechaDesde AND e.fechaFin <= :fechaHasta " +
            "ORDER BY e.fechaInicio DESC")
    List<Evento> buscarConFiltros(@Param("tipo") Evento.Tipo tipo,
                                  @Param("modalidad") Evento.Modalidad modalidad,
                                  @Param("estado") Evento.Estado estado,
                                  @Param("fechaDesde") LocalDateTime fechaDesde,
                                  @Param("fechaHasta") LocalDateTime fechaHasta);

}
