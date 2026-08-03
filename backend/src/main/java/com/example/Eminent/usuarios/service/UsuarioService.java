package com.example.Eminent.usuarios.service;

import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.auth.JwtUtil;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern PATRON_CONTRASENA = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");

    @Autowired private UsuarioRepository repo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuditoriaService auditoriaService;

    /**
     * Crea un nuevo usuario validando todos los campos requeridos,
     * incluyendo formato de teléfono y fortaleza de contraseña.
     */
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

        if (nuevo.getTelefono() != null && !nuevo.getTelefono().isBlank() &&
                !PATRON_TELEFONO.matcher(nuevo.getTelefono()).matches()) {
            throw new IllegalArgumentException("Formato de teléfono inválido. Use +573001234567 o 3001234567");
        }

        if (!PATRON_CONTRASENA.matcher(nuevo.getContrasena()).matches()) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una letra mayúscula, una minúscula y un dígito");
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

    /**
     * Lista usuarios paginados, filtrando opcionalmente por rol.
     */
    public Page<Usuario> listar(String rol, Pageable pageable) {
        if (rol != null && !rol.isBlank()) {
            return repo.findByRol(Usuario.Rol.valueOf(rol.toUpperCase()), pageable);
        }
        return repo.findAll(pageable);
    }

    /**
     * Obtiene un usuario por su identificador único.
     */
    public Usuario obtenerPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    /**
     * Elimina permanentemente un usuario del sistema.
     */
    public void eliminar(Long id) {
        Usuario existente = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (existente.getRol() == Usuario.Rol.ADMIN) {
            throw new IllegalArgumentException("No se puede eliminar un usuario Administrador");
        }

        repo.deleteById(id);

        auditoriaService.registrar(existente, Auditoria.Accion.DESACTIVAR, Auditoria.TipoAfectado.USUARIO,
                existente.getId(),
                "Eliminación del usuario " + existente.getCorreo(),
                null);
    }

    /**
     * Edita los datos de un usuario existente con validación de teléfono y contraseña opcional.
     */
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
        if (datos.getTelefono() != null) {
            if (!datos.getTelefono().isBlank() && !PATRON_TELEFONO.matcher(datos.getTelefono()).matches()) {
                throw new IllegalArgumentException("Formato de teléfono inválido. Use +573001234567 o 3001234567");
            }
            existente.setTelefono(datos.getTelefono());
        }
        if (datos.getRol() != null && existente.getRol() != Usuario.Rol.ADMIN) {
            existente.setRol(datos.getRol());
        }

        if (datos.getContrasena() != null && !datos.getContrasena().isBlank()) {
            if (!PATRON_CONTRASENA.matcher(datos.getContrasena()).matches()) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una letra mayúscula, una minúscula y un dígito");
            }
            existente.setContrasena(passwordEncoder.encode(datos.getContrasena()));
        }

        Usuario actualizado = repo.save(existente);

        auditoriaService.registrar(actualizado, Auditoria.Accion.EDITAR, Auditoria.TipoAfectado.USUARIO,
                actualizado.getId(), "Edición de usuario " + actualizado.getCorreo(), null);

        return actualizado;
    }

    /**
     * Cambia el estado  de un usuario.
     */
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

    /**
     * Autentica a un usuario y genera un token JWT.
     */
    public String login(String correo, String contrasena) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(correo, contrasena));
        } catch (Exception e) {
            auditoriaService.registrar(null, Auditoria.Accion.LOGIN_FALLIDO, Auditoria.TipoAfectado.USUARIO,
                    null, "Intento de inicio de sesión fallido para el correo: " + correo, null);
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        }
        Usuario usuario = repo.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));

        auditoriaService.registrar(usuario, Auditoria.Accion.LOGIN_EXITOSO, Auditoria.TipoAfectado.USUARIO,
                usuario.getId(), "Inicio de sesión exitoso", null);

        return jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol().name());
    }
}