package com.example.Eminent.eventos.controller;

import com.example.Eminent.eventos.dto.EventoDTO;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.EventoMonitor;
import com.example.Eminent.eventos.repository.EventoMonitorRepository;
import com.example.Eminent.eventos.service.EventosService;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired private EventosService eventosService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private EventoMonitorRepository eventoMonitorRepository;

    /**
     * Obtiene los detalles completos de un evento por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Evento evento = eventosService.obtenerPorId(id);
        return ResponseEntity.ok(toDTO(evento));
    }

    /**
     * Lista los monitores asignados a un evento específico.
     */
    @GetMapping("/{id}/monitores")
    public ResponseEntity<?> listarMonitores(@PathVariable Long id) {
        List<EventoMonitor> asignaciones = eventoMonitorRepository.findByEvento_Id(id);
        List<Map<String, Object>> result = asignaciones.stream().map(a -> Map.<String, Object>of(
                "monitorId", a.getMonitor().getId(),
                "monitorNombre", a.getMonitor().getNombre(),
                "monitorCorreo", a.getMonitor().getCorreo(),
                "fechaAsignacion", a.getFechaAsignacion()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Crea un nuevo evento. Solo accesible para ADMIN y OPERADOR.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> crear(@RequestBody EventoDTO dto) {
        Usuario usuario = obtenerUsuarioActual();
        Evento creado = eventosService.crear(dto, usuario);
        return ResponseEntity.ok(toDTO(creado));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<Page<EventoDTO>> listarParaAdminOperador(
            @RequestParam(required = false) Evento.Tipo tipo,
            @RequestParam(required = false) Evento.Modalidad modalidad,
            @RequestParam(required = false) Evento.Estado estado,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fechaDesde,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fechaHasta,
            Pageable pageable) {

        Page<Evento> pagina = eventosService.listarConFiltros(tipo, modalidad, estado, fechaDesde, fechaHasta, pageable);
        return ResponseEntity.ok(pagina.map(this::toDTO));
    }

    /**
     * Lista los eventos asignados al MONITOR que ha hecho la petición, con paginación.
     */
    @GetMapping("/mis-eventos")
    @PreAuthorize("hasRole('MONITOR')")
    public ResponseEntity<Page<EventoDTO>> listarParaMonitor(Pageable pageable) {
        Usuario usuario = obtenerUsuarioActual();
        Page<Evento> pagina = eventosService.listarParaMonitor(usuario.getId(), pageable);
        return ResponseEntity.ok(pagina.map(this::toDTO));
    }


    @GetMapping("/publicos")
    public ResponseEntity<Page<EventoDTO>> listarPublicos(Pageable pageable) {
        Page<Evento> pagina = eventosService.listarPublicos(pageable);
        return ResponseEntity.ok(pagina.map(this::toDTO));
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
        dto.setInscritos(inscripcionRepository.countByEventoIdAndEstado(evento.getId(), Inscripcion.Estado.ACTIVA));
        dto.setEstado(evento.getEstado().name());
        dto.setCreadoPor(evento.getCreadoPor().getId());
        dto.setFechaCreacion(evento.getFechaCreacion());
        return dto;
    }
}