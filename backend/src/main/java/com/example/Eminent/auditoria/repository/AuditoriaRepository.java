package com.example.Eminent.auditoria.repository;

import com.example.Eminent.auditoria.entity.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Auditoria}.
 * Proporciona consultas personalizadas con filtros por usuario, tipo de entidad afectada,
 * rango de fecha y paginación. Incluye una variante sin paginación para exportaciones.
 */
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    /**
     * Busca registros de auditoría con filtros opcionales por usuario, tipo de entidad
     * afectada y rango de fecha. Los resultados se ordenan por fecha descendente.
     * Los parámetros con valor null se ignoran en el filtro.
     */
    @Query("SELECT a FROM Auditoria a WHERE " +
            "(:usuarioId IS NULL OR a.usuario.id = :usuarioId) AND " +
            "(:tipoAfectado IS NULL OR a.tipoAfectado = :tipoAfectado) AND " +
            "a.fechaHora >= :fechaInicio AND a.fechaHora <= :fechaFin " +
            "ORDER BY a.fechaHora DESC")
    Page<Auditoria> buscarConFiltros(@Param("usuarioId") Long usuarioId,
                                                           @Param("tipoAfectado") Auditoria.TipoAfectado tipoAfectado,
                                                           @Param("fechaInicio") LocalDateTime fechaInicio,
                                                           @Param("fechaFin") LocalDateTime fechaFin,
                                                           Pageable pageable);

    /**
     * Variante de búsqueda sin paginación. Útil para exportaciones o listados completos.
     * Aplica los mismos filtros que {@link #buscarConFiltros(Long, Auditoria.TipoAfectado, LocalDateTime, LocalDateTime, Pageable)}.
     */
    @Query("SELECT a FROM Auditoria a WHERE " +
            "(:usuarioId IS NULL OR a.usuario.id = :usuarioId) AND " +
            "(:tipoAfectado IS NULL OR a.tipoAfectado = :tipoAfectado) AND " +
            "a.fechaHora >= :fechaInicio AND a.fechaHora <= :fechaFin " +
            "ORDER BY a.fechaHora DESC")
    List<Auditoria> buscarConFiltrosSinPage(@Param("usuarioId") Long usuarioId,
                                                           @Param("tipoAfectado") Auditoria.TipoAfectado tipoAfectado,
                                                           @Param("fechaInicio") LocalDateTime fechaInicio,
                                                           @Param("fechaFin") LocalDateTime fechaFin);
}