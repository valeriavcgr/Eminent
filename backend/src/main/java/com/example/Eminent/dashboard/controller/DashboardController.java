package com.example.Eminent.dashboard.controller;

import com.example.Eminent.dashboard.dto.DashboardDTO;
import com.example.Eminent.dashboard.service.DashboardService;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private DashboardService service;
    @Autowired private UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<DashboardDTO> resumen(
            @RequestParam(required = false) Evento.Tipo tipo,
            @RequestParam(required = false) Evento.Modalidad modalidad,
            @RequestParam(required = false) Evento.Estado estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {

        Usuario usuario = usuarioActual();
        return ResponseEntity.ok(service.obtenerResumen(tipo, modalidad, estado, fechaDesde, fechaHasta, usuario));
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}