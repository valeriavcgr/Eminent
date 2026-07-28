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
import java.util.Map;
import java.util.stream.Collectors;

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
        return ResponseEntity.ok(toDTO(creado));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<List<EventoDTO>> listarParaAdminOperador() {
        List<EventoDTO> lista = eventosService.listarParaAdminOperador().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/mis-eventos")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<List<EventoDTO>> listarParaMonitor() {
        Usuario usuario = obtenerUsuarioActual();
        List<EventoDTO> lista = eventosService.listarParaMonitor(usuario.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/publicos")
    public ResponseEntity<List<EventoDTO>> listarPublicos() {
        List<EventoDTO> lista = eventosService.listarPublicos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody EventoDTO dto) {
        Usuario usuario = obtenerUsuarioActual();
        Evento actualizado = eventosService.editar(id, dto, usuario);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    @PostMapping("/{id}/monitores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> asignarMonitor(@PathVariable Long id, @RequestParam Long monitorId) {
        Usuario usuario = obtenerUsuarioActual();
        EventoMonitor asignacion = eventosService.asignarMonitor(id, monitorId, usuario);
        return ResponseEntity.ok(Map.of(
                "eventoId", asignacion.getEvento().getId(),
                "monitorId", asignacion.getMonitor().getId(),
                "monitorNombre", asignacion.getMonitor().getNombre(),
                "fechaAsignacion", asignacion.getFechaAsignacion()
        ));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        Usuario usuario = obtenerUsuarioActual();
        Evento cancelado = eventosService.cancelar(id, usuario);
        return ResponseEntity.ok(toDTO(cancelado));
    }

    private Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correo));
    }

    private EventoDTO toDTO(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setId(evento.getId());
        dto.setNombre(evento.getNombre());
        dto.setTipo(evento.getTipo().name());
        dto.setModalidad(evento.getModalidad().name());
        dto.setDescripcion(evento.getDescripcion());
        dto.setFechaInicio(evento.getFechaInicio());
        dto.setFechaFin(evento.getFechaFin());
        dto.setAforo(evento.getAforo());
        dto.setEstado(evento.getEstado().name());
        dto.setCreadoPor(evento.getCreadoPor().getId());
        dto.setFechaCreacion(evento.getFechaCreacion());
        return dto;
    }
}