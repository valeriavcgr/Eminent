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

/**
 * Controlador REST para la importación masiva de participantes mediante archivos CSV.
 * Proporciona dos endpoints: previsualización (validación sin persistir) y confirmación
 * (persistencia de participantes e inscripciones válidos). Solo accesible para ADMIN y OPERADOR.
 */
@RestController
@RequestMapping("/api/eventos")
public class CsvImportController {

    @Autowired private CsvImportService csvImportService;
    @Autowired private UsuarioRepository usuarioRepository;

    /**
     * Previsualiza el contenido de un archivo CSV para un evento dado.
     * Valida cada fila y devuelve una lista con el estado de validación de cada participante
     * sin persistir ningún dato. Solo accesible para ADMIN y OPERADOR.
     */
    @PostMapping("/{id}/importar-csv/previsualizar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<List<FilaCsvDTO>> previsualizar(@PathVariable Long id,
                                                                              @RequestParam("archivo") MultipartFile archivo) throws Exception {
        return ResponseEntity.ok(csvImportService.previsualizar(archivo, id));
    }

    /**
     * Confirma e importa los participantes de un archivo CSV a un evento.
     * Crea participantes nuevos e inscripciones para las filas válidas.
     * Solo accesible para ADMIN y OPERADOR.
     */
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