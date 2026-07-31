package com.example.Eminent.participacion.controller;

import com.example.Eminent.participacion.dto.FichaHistorialDTO;
import com.example.Eminent.participacion.service.ParticipacionService;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para consultar la ficha histórica de participantes.
 * Permite buscar inscripciones y asistencias pasadas de un participante
 * filtrando por documento, correo o nombre. Solo accesible para ADMIN y OPERADOR.
 */
@RestController
@RequestMapping("/api/participantes")
public class HistorialController {

    @Autowired private ParticipacionService participacionService;
    @Autowired private UsuarioRepository usuarioRepository;

    /**
     * Consulta la ficha histórica de un participante con sus inscripciones y asistencias.
     * Los filtros (documento, correo, nombre) son opcionales y se combinan con AND.
     * Solo accesible para ADMIN y OPERADOR.
     */
    @GetMapping("/historial")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<List<FichaHistorialDTO>> historial(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String nombre) {
        return ResponseEntity.ok(participacionService.historial(documento, correo, nombre));
    }

    /** Obtiene el usuario actualmente autenticado desde el contexto de seguridad. */
    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}