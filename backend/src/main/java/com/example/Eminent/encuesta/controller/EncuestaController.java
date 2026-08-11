package com.example.Eminent.encuesta.controller;

import com.example.Eminent.encuesta.dto.EncuestaComentarioDTO;
import com.example.Eminent.encuesta.dto.EncuestaDTO;
import com.example.Eminent.encuesta.service.EncuestaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/encuestas")
public class EncuestaController {

    @Autowired private EncuestaService service;

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody EncuestaDTO datos) {
        service.registrar(datos);
        return ResponseEntity.ok(Map.of("mensaje", "¡Gracias por tu opinión!"));
    }

    @GetMapping("/evento/{eventoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('MONITOR')")
    public ResponseEntity<Page<EncuestaComentarioDTO>> listarPorEvento(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Integer calificacion,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.listarPorEvento(eventoId, calificacion, pageable));
    }

    @GetMapping("/evento/{eventoId}/promedio")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('MONITOR')")
    public ResponseEntity<?> obtenerPromedioEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(Map.of("promedio", service.obtenerPromedioEvento(eventoId)));
    }
}