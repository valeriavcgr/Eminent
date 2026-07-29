package com.example.Eminent.usuarios.service;

import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.auth.JwtUtil;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired private UsuarioRepository repo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuditoriaService auditoriaService;

    public Usuario crear(Usuario nuevo) {
        if (nuevo.getNombre() == null || nuevo.getNombre().isBlank() ||
                nuevo.getApellido() == null || nuevo.getApellido().isBlank() ||
                nuevo.getCorreo() == null || nuevo.getCorreo().isBlank() ||
                nuevo.getContrasena() == null || nuevo.getContrasena().isBlank() ||
                nuevo.getRol() == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios");
        }

        if (nuevo.getRol() == Usuario.Rol.ADMIN) {
            throw new IllegalArgumentException("Solo se pueden crear usuarios con rol MONITOR u OPERADOR");
        }

        if (repo.existsByCorreo(nuevo.getCorreo())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado");
        }

        nuevo.setContrasena(passwordEncoder.encode(nuevo.getContrasena()));
        nuevo.setEstado(Usuario.Estado.ACTIVO);
        Usuario guardado = repo.save(nuevo);

        auditoriaService.registrar(guardado, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.USUARIO,
                guardado.getId(),
                "Creación de usuario nuevo con rol " + guardado.getRol() + " y correo " + guardado.getCorreo(),
                null);

        return guardado;
    }

    public List<Usuario> listar(String rol) {
        if (rol != null && !rol.isBlank()) {
            return repo.findByRol(Usuario.Rol.valueOf(rol.toUpperCase()));
        }
        return repo.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public Usuario editar(Long id, Usuario datos) {
        Usuario existente = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (existente.getRol() == Usuario.Rol.ADMIN && datos.getRol() != null
                && datos.getRol() != existente.getRol()) {
            throw new IllegalArgumentException("No se puede editar el rol de un usuario Administrador");
        }

        if (datos.getCorreo() != null && !datos.getCorreo().equals(existente.getCorreo())
                && repo.existsByCorreo(datos.getCorreo())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado");
        }

        if (datos.getNombre() != null) existente.setNombre(datos.getNombre());
        if (datos.getApellido() != null) existente.setApellido(datos.getApellido());
        if (datos.getCorreo() != null) existente.setCorreo(datos.getCorreo());
        if (datos.getTelefono() != null) existente.setTelefono(datos.getTelefono());
        if (datos.getRol() != null && existente.getRol() != Usuario.Rol.ADMIN) {
            existente.setRol(datos.getRol());
        }

        Usuario actualizado = repo.save(existente);

        auditoriaService.registrar(actualizado, Auditoria.Accion.EDITAR, Auditoria.TipoAfectado.USUARIO,
                actualizado.getId(), "Edición de usuario " + actualizado.getCorreo(), null);

        return actualizado;
    }

    public Usuario cambiarEstado(Long id, String nuevoEstado) {
        Usuario usuario = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setEstado(Usuario.Estado.valueOf(nuevoEstado.toUpperCase()));
        Usuario actualizado = repo.save(usuario);

        auditoriaService.registrar(actualizado,
                actualizado.getEstado() == Usuario.Estado.ACTIVO ? Auditoria.Accion.EDITAR : Auditoria.Accion.DESACTIVAR,
                Auditoria.TipoAfectado.USUARIO, actualizado.getId(),
                "Cambio de estado de usuario " + actualizado.getCorreo() + " a " + actualizado.getEstado(), null);

        return actualizado;
    }

    public String login(String correo, String contrasena) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(correo, contrasena));
        } catch (Exception e) {
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        }
        Usuario usuario = repo.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));
        return jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol().name());
    }
}