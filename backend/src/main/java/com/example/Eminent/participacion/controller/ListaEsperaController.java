package com.example.Eminent.participacion.controller;

import com.example.Eminent.participacion.dto.InscripcionEsperaDTO;
import com.example.Eminent.participacion.service.ParticipacionService;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de la lista de espera de eventos.
 * Permite consultar la cola de espera de un evento y promover (confirmar)
 * inscripciones de espera a activas cuando hay cupo disponible.
 * Solo accesible para ADMIN y OPERADOR.
 */
@RestController
@RequestMapping("/api")
public class ListaEsperaController {

    @Autowired private ParticipacionService service;
    @Autowired private UsuarioRepository usuarioRepository;

    /**
     * Consulta la lista de espera de un evento específico.
     * Devuelve los participantes en orden de posición en la cola.
     * Solo accesible para ADMIN y OPERADOR.
     */
    @GetMapping("/eventos/{id}/lista-espera")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<List<InscripcionEsperaDTO>> consultarCola(@PathVariable Long id) {
        return ResponseEntity.ok(service.consultarCola(id));
    }

    /**
     * Promueve la primera inscripción de la lista de espera del evento a estado ACTIVO,
     * liberando un cupo. Solo accesible para ADMIN y OPERADOR.
     */
    @PostMapping("/inscripciones/{id}/promover")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> promover(@PathVariable Long id) throws Exception {
        Usuario ejecutor = obtenerUsuarioActual();
        service.promover(id, ejecutor);
        return ResponseEntity.ok(Map.of("mensaje", "Inscripción promovida correctamente"));
    }

    /** Obtiene el usuario actualmente autenticado desde el contexto de seguridad. */
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}

