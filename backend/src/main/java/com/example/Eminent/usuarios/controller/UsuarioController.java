package com.example.Eminent.usuarios.controller;

import com.example.Eminent.usuarios.dto.LoginRequest;
import com.example.Eminent.usuarios.dto.UsuarioDTO;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    @Autowired private UsuarioService service;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = service.login(request.getCorreo(), request.getContrasena());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> crear(@RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(service.crear(usuario)));
    }

    @GetMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
        Usuario usuario = service.obtenerPorId(id);
        return ResponseEntity.ok(toDTO(usuario));
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> listar(@RequestParam(required = false) String rol) {
        List<UsuarioDTO> lista = service.listar(rol).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> editar(@PathVariable Long id, @RequestBody Usuario datos) {
        return ResponseEntity.ok(toDTO(service.editar(id, datos)));
    }

    @PatchMapping("/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(toDTO(service.cambiarEstado(id, body.get("estado"))));
    }

    private UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setApellido(u.getApellido());
        dto.setCorreo(u.getCorreo());
        dto.setTelefono(u.getTelefono());
        dto.setRol(u.getRol().name());
        dto.setEstado(u.getEstado().name());
        dto.setFechaCreacion(u.getFechaCreacion());
        return dto;
    }
}