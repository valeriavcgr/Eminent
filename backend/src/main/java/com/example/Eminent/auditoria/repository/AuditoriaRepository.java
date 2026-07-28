package com.example.Eminent.auditoria.repository;

import com.example.Eminent.auditoria.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    @Query("SELECT a FROM Auditoria a WHERE " +
            "(:usuarioId IS NULL OR a.usuario.id = :usuarioId) AND " +
            "(:tipoAfectado IS NULL OR a.tipoAfectado = :tipoAfectado) AND " +
            "a.fechaHora >= :fechaInicio AND a.fechaHora <= :fechaFin " +
            "ORDER BY a.fechaHora DESC")
    List<Auditoria> buscarConFiltros(@Param("usuarioId") Long usuarioId,
                                     @Param("tipoAfectado") Auditoria.TipoAfectado tipoAfectado,
                                     @Param("fechaInicio") LocalDateTime fechaInicio,
                                     @Param("fechaFin") LocalDateTime fechaFin);
}