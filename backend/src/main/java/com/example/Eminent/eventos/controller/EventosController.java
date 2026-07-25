package com.example.Eminent.eventos.controller;

import com.example.Eminent.eventos.dto.EventoDTO;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.EventoMonitor;
import com.example.Eminent.eventos.service.EventosService;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventosController {

    @Autowired
    private EventosService eventosService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> crear(@RequestBody EventoDTO dto) {
        Usuario usuario = obtenerUsuarioActual();
        Evento creado = eventosService.crear(dto, usuario);
        return ResponseEntity.ok(creado);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<List<Evento>> listarParaAdminOperador() {
        return ResponseEntity.ok(eventosService.listarParaAdminOperador());
    }

    @GetMapping("/mis-eventos")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<List<Evento>> listarParaMonitor() {
        Usuario usuario = obtenerUsuarioActual();
        return ResponseEntity.ok(eventosService.listarParaMonitor(usuario.getId()));
    }

    @GetMapping("/publicos")
    public ResponseEntity<List<Evento>> listarPublicos() {
        return ResponseEntity.ok(eventosService.listarPublicos());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody EventoDTO dto) {
        Usuario usuario = obtenerUsuarioActual();
        Evento actualizado = eventosService.editar(id, dto, usuario);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/monitores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> asignarMonitor(@PathVariable Long id, @RequestParam Long monitorId) {
        Usuario usuario = obtenerUsuarioActual();
        EventoMonitor asignacion = eventosService.asignarMonitor(id, monitorId, usuario);
        return ResponseEntity.ok(asignacion);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        Usuario usuario = obtenerUsuarioActual();
        Evento cancelado = eventosService.cancelar(id, usuario);
        return ResponseEntity.ok(cancelado);
    }

    private Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correo));
    }
}