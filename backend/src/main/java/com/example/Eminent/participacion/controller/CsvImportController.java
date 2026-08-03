package com.example.Eminent.participacion.controller;

import com.example.Eminent.participacion.dto.FilaCsvDTO;
import com.example.Eminent.participacion.service.CsvImportService;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class CsvImportController {

    @Autowired private CsvImportService csvImportService;
    @Autowired private UsuarioRepository usuarioRepository;

    @PostMapping("/{id}/importar-csv/previsualizar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<List<FilaCsvDTO>> previsualizar(@PathVariable Long id,
                                                                              @RequestParam("archivo") MultipartFile archivo) throws Exception {
        return ResponseEntity.ok(csvImportService.previsualizar(archivo, id));
    }

    @PostMapping("/{id}/importar-csv/confirmar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> confirmar(@PathVariable Long id,
                                              @RequestParam("archivo") MultipartFile archivo) throws Exception {
        Usuario ejecutor = obtenerUsuarioActual();
        return ResponseEntity.ok(csvImportService.confirmar(archivo, id, ejecutor));
    }

    /** Obtiene el usuario actualmente autenticado desde el contexto de seguridad. */
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}