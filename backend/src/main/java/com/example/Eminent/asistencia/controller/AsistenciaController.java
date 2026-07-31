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

/**
 * Controlador REST para el registro de asistencia de participantes en eventos.
 * Permite listar participantes de un evento y registrar asistencia de forma manual
 * (por ID de inscripción) o mediante escaneo de código QR. Solo MONITOR puede registrar.
 */
@RestController
@RequestMapping("/api")
public class AsistenciaController {

    @Autowired private AsistenciaService service;
    @Autowired private UsuarioRepository usuarioRepository;

    /**
     * Lista todos los participantes de un evento con su estado de asistencia.
     * Accesible por ADMIN, OPERADOR y MONITOR.
     */
    @GetMapping("/eventos/{id}/participantes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('MONITOR')")
    public ResponseEntity<ListadoAsistenciaDTO> listar(@PathVariable Long id) {
        Usuario usuario = usuarioActual();
        return ResponseEntity.ok(service.listarParticipantes(id, usuario.getId()));
    }

    /**
     * Registra la asistencia de un participante de forma manual proporcionando
     * el ID de inscripción. Solo accesible para MONITOR.
     */
    @PostMapping("/asistencias/manual")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<?> registrarManual(@RequestBody Map<String, Long> body) {
        Usuario monitor = usuarioActual();
        service.registrarManual(body.get("inscripcionId"), monitor);
        return ResponseEntity.ok(Map.of("mensaje", "Asistencia registrada correctamente"));
    }

    /**
     * Registra la asistencia de un participante mediante el escaneo de un
     * código QR codificado con el ID de inscripción y evento. Solo accesible para MONITOR.
     */
    @PostMapping("/asistencias/qr")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<?> registrarPorQr(@RequestBody Map<String, Object> body) {
        Usuario monitor = usuarioActual();
        String contenidoQr = (String) body.get("contenidoQr");
        Long eventoId = Long.valueOf(body.get("eventoId").toString());
        service.registrarPorQr(contenidoQr, eventoId, monitor);
        return ResponseEntity.ok(Map.of("mensaje", "Asistencia registrada correctamente vía QR"));
    }

    /** Obtiene el usuario actualmente autenticado desde el contexto de seguridad. */
    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}