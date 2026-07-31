package com.example.Eminent.auditoria.controller;

import com.example.Eminent.auditoria.dto.AuditoriaDTO;
import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador REST para la gestión de registros de auditoría.
 * Proporciona un endpoint de consulta avanzada con filtros por usuario,
 * tipo de entidad afectada y rango de fechas. Solo accesible por ADMIN.
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    @Autowired private AuditoriaService service;

    /**
     * Consulta registros de auditoría con filtros opcionales por usuario, tipo de entidad
     * afectada y rango de fechas. Los resultados se devuelven paginados.
     * Solo accesible para ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditoriaDTO>> consultar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Auditoria.TipoAfectado tipoAfectado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        return ResponseEntity.ok(service.consultar(usuarioId, tipoAfectado, fechaInicio, fechaFin, pageable));
    }
}