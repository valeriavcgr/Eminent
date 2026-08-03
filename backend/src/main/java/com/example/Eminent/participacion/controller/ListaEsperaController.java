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


@RestController
@RequestMapping("/api")
public class ListaEsperaController {

    @Autowired private ParticipacionService service;
    @Autowired private UsuarioRepository usuarioRepository;

    @GetMapping("/eventos/{id}/lista-espera")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<List<InscripcionEsperaDTO>> consultarCola(@PathVariable Long id) {
        return ResponseEntity.ok(service.consultarCola(id));
    }

    @PostMapping("/inscripciones/{id}/promover")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> promover(@PathVariable Long id) throws Exception {
        Usuario ejecutor = obtenerUsuarioActual();
        service.promover(id, ejecutor);
        return ResponseEntity.ok(Map.of("mensaje", "Inscripción promovida correctamente"));
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}

