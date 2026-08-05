package com.example.Eminent.encuesta.controller;

import com.example.Eminent.encuesta.dto.EncuestaComentarioDTO;
import com.example.Eminent.encuesta.dto.EncuestaDTO;
import com.example.Eminent.encuesta.service.EncuestaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/encuestas")
public class EncuestaController {

    @Autowired private EncuestaService service;

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody EncuestaDTO datos) {
        service.registrar(datos);
        return ResponseEntity.ok(Map.of("mensaje", "¡Gracias por tu opinión!"));
    }

    @GetMapping("/evento/{eventoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('MONITOR')")
    public ResponseEntity<List<EncuestaComentarioDTO>> listarPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(service.listarPorEvento(eventoId));
    }
}