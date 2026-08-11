package com.example.Eminent.eventos.repository;

import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.Evento.Estado;
import com.example.Eminent.eventos.entity.Evento.Tipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    /** Busca un evento creado por el usuario con el ID proporcionado. */
    Optional<Evento> findByCreadoPor_Id(Long creadoPorId);

    /** Lista paginada de eventos filtrados por estado. */
    Page<Evento> findByEstado(Estado estado, Pageable pageable);

    /** Lista paginada de eventos filtrados por una lista de estados. */
    Page<Evento> findByEstadoIn(List<Estado> estados, Pageable pageable);

    /** Verifica si existe un evento con el mismo nombre y tipo. */
    boolean existsByNombreAndTipo(String nombre, Tipo tipo);

    /** Lista eventos con estado dado cuya fecha de inicio es igual o anterior a la fecha proporcionada. */
    List<Evento> findByEstadoAndFechaInicioLessThanEqual(Estado estado, LocalDateTime fecha);

    /** Lista eventos con estado dado cuya fecha de finalización es igual o anterior a la fecha proporcionada. */
    List<Evento> findByEstadoAndFechaFinLessThanEqual(Estado estado, LocalDateTime fecha);

    /**
     * Búsqueda avanzada de eventos con filtros opcionales
     */
    @Query("SELECT e FROM Evento e WHERE " +
            "(:tipo IS NULL OR e.tipo = :tipo) AND " +
            "(:modalidad IS NULL OR e.modalidad = :modalidad) AND " +
            "(:estado IS NULL OR e.estado = :estado) AND " +
            "(:creadoPorId IS NULL OR e.creadoPor.id = :creadoPorId) AND " +
            "e.fechaInicio >= :fechaDesde AND e.fechaFin <= :fechaHasta " +
            "ORDER BY e.fechaInicio DESC")
    Page<Evento> buscarConFiltros(@Param("tipo") Evento.Tipo tipo,
                                                  @Param("modalidad") Evento.Modalidad modalidad,
                                                  @Param("estado") Evento.Estado estado,
                                                  @Param("fechaDesde") LocalDateTime fechaDesde,
                                                  @Param("fechaHasta") LocalDateTime fechaHasta,
                                                  @Param("creadoPorId") Long creadoPorId,
                                                  Pageable pageable);
}