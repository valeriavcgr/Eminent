package com.example.Eminent.usuarios.controller;

import com.example.Eminent.usuarios.dto.LoginRequest;
import com.example.Eminent.usuarios.dto.UsuarioDTO;
import com.example.Eminent.usuarios.dto.UsuarioRequest;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api")
public class UsuarioController {

    @Autowired private UsuarioService service;

    /**
     * Autentica al usuario con correo y contraseña, devolviendo un token JWT
     * si las credenciales son válidas.
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = service.login(request.getCorreo(), request.getContrasena());
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Crea un nuevo usuario. Solo accesible por ADMIN.
     * Aplica validaciones de teléfono y contraseña antes de persistir.
     */
    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> crear(@RequestBody UsuarioRequest usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(service.crear(usuario)));
    }

    /**
     * Obtiene los detalles de un usuario específico por su ID. Solo accesible por ADMIN.
     */
    @GetMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
        Usuario usuario = service.obtenerPorId(id);
        return ResponseEntity.ok(toDTO(usuario));
    }

    /**
     * Lista todos los usuarios con paginación y filtro opcional por rol. Solo accesible por ADMIN.
     */
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioDTO>> listar(@RequestParam(required = false) String rol, Pageable pageable) {
        Page<Usuario> pagina = service.listar(rol, pageable);
        return ResponseEntity.ok(pagina.map(this::toDTO));
    }

    /**
     * Obtiene los datos del propio usuario autenticado, para la pantalla "Mi Perfil".
     * Solo accesible por ADMIN (es el único rol que puede crear/editar usuarios y, por lo
     * tanto, editarse a sí mismo).
     */
    @GetMapping("/usuarios/yo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> obtenerActual() {
        return ResponseEntity.ok(toDTO(usuarioActual()));
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return service.obtenerPorCorreo(auth.getName());
    }

    /**
     * Actualiza los datos de un usuario existente. Solo accesible por ADMIN.
     * Si se proporcionan teléfono o contraseña, se validan con las reglas actuales.
     */
    @PutMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> editar(@PathVariable Long id, @RequestBody UsuarioRequest datos) {
        return ResponseEntity.ok(toDTO(service.editar(id, datos)));
    }

    /**
     * Cambia el estado de un usuario. Solo accesible por ADMIN.
     */
    @PatchMapping("/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(toDTO(service.cambiarEstado(id, body.get("estado"))));
    }

    /** Convierte una entidad Usuario a su dto correspondiente para la respuesta API. */
    private UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setApellido(u.getApellido());
        dto.setCorreo(u.getCorreo());
        dto.setTelefono(u.getTelefono());
        dto.setRoles(u.getRoles().stream().map(r -> r.getNombre().name()).sorted().toList());
        dto.setEstado(u.getEstado().name());
        dto.setFechaCreacion(u.getFechaCreacion());
        return dto;
    }
}