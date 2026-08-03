package com.example.Eminent.asistencia.controller;

import com.example.Eminent.asistencia.dto.ListadoAsistenciaDTO;
import com.example.Eminent.asistencia.service.AsistenciaService;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AsistenciaController {

    @Autowired private AsistenciaService service;
    @Autowired private UsuarioRepository usuarioRepository;

    @GetMapping("/eventos/{id}/participantes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('MONITOR')")
    public ResponseEntity<ListadoAsistenciaDTO> listar(@PathVariable Long id) {
        Usuario usuario = usuarioActual();
        return ResponseEntity.ok(service.listarParticipantes(id, usuario));
    }

    @PostMapping("/asistencias/manual")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<?> registrarManual(@RequestBody Map<String, Long> body) {
        Usuario monitor = usuarioActual();
        service.registrarManual(body.get("inscripcionId"), monitor);
        return ResponseEntity.ok(Map.of("mensaje", "Asistencia registrada correctamente"));
    }

    @PostMapping("/asistencias/qr")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<?> registrarPorQr(@RequestBody Map<String, Object> body) {
        Usuario monitor = usuarioActual();
        String contenidoQr = (String) body.get("contenidoQr");
        Long eventoId = Long.valueOf(body.get("eventoId").toString());
        service.registrarPorQr(contenidoQr, eventoId, monitor);
        return ResponseEntity.ok(Map.of("mensaje", "Asistencia registrada correctamente vía QR"));
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}