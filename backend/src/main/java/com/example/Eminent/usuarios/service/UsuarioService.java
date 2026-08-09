package com.example.Eminent.usuarios.service;

import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.auth.JwtUtil;
import com.example.Eminent.usuarios.dto.UsuarioRequest;
import com.example.Eminent.usuarios.entity.Rol;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.RolRepository;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern PATRON_CONTRASENA = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");

    @Autowired private UsuarioRepository repo;
    @Autowired private RolRepository rolRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuditoriaService auditoriaService;

    /** Resuelve una lista de nombres de rol (texto) a las entidades Rol del catálogo. */
    private Set<Rol> resolverRolesDesdeNombres(List<String> nombres) {
        Set<Rol> resultado = new HashSet<>();
        for (String nombre : nombres) {
            Usuario.Rol valor;
            try {
                valor = Usuario.Rol.valueOf(nombre.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Rol inválido: " + nombre);
            }
            resultado.add(rolRepo.findByNombre(valor).orElseThrow());
        }
        return resultado;
    }

    /**
     * Crea un nuevo usuario validando todos los campos requeridos,
     * incluyendo formato de teléfono, fortaleza de contraseña y al menos un rol.
     */
    public Usuario crear(UsuarioRequest datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank() ||
                datos.getApellido() == null || datos.getApellido().isBlank() ||
                datos.getCorreo() == null || datos.getCorreo().isBlank() ||
                datos.getContrasena() == null || datos.getContrasena().isBlank() ||
                datos.getRoles() == null || datos.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios, incluyendo al menos un rol");
        }

        if (datos.getTelefono() != null && !datos.getTelefono().isBlank() &&
                !PATRON_TELEFONO.matcher(datos.getTelefono()).matches()) {
            throw new IllegalArgumentException("Formato de teléfono inválido. Use +573001234567 o 3001234567");
        }

        if (!PATRON_CONTRASENA.matcher(datos.getContrasena()).matches()) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una letra mayúscula, una minúscula y un dígito");
        }

        if (repo.existsByCorreo(datos.getCorreo())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(datos.getNombre());
        nuevo.setApellido(datos.getApellido());
        nuevo.setCorreo(datos.getCorreo());
        nuevo.setTelefono(datos.getTelefono());
        nuevo.setContrasena(passwordEncoder.encode(datos.getContrasena()));
        nuevo.setEstado(Usuario.Estado.ACTIVO);
        nuevo.getRoles().addAll(resolverRolesDesdeNombres(datos.getRoles()));

        Usuario guardado = repo.save(nuevo);

        auditoriaService.registrar(guardado, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.USUARIO,
                guardado.getId(),
                "Creación de usuario nuevo con roles " + datos.getRoles() + " y correo " + guardado.getCorreo(),
                null);

        return guardado;
    }

    /**
     * Lista usuarios paginados, filtrando opcionalmente por rol (usuarios que tengan ese rol
     * entre los suyos).
     */
    public Page<Usuario> listar(String rol, Pageable pageable) {
        if (rol != null && !rol.isBlank()) {
            Usuario.Rol nombre;
            try {
                nombre = Usuario.Rol.valueOf(rol.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Rol inválido: " + rol);
            }
            return repo.findByRolesNombre(nombre, pageable);
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
     * Obtiene un usuario por su correo electrónico (usado para resolver "mi perfil"
     * a partir del usuario autenticado en la sesión actual).
     */
    public Usuario obtenerPorCorreo(String correo) {
        return repo.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    /**
     * Edita los datos de un usuario existente con validación de teléfono y contraseña opcional.
     * Un ADMIN puede asignar o quitar cualquier rol (incluido ADMIN) a cualquier usuario,
     * incluso a sí mismo — esta acción ya está reservada a administradores vía @PreAuthorize.
     */
    public Usuario editar(Long id, UsuarioRequest datos) {
        Usuario existente = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

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
        if (datos.getRoles() != null) {
            if (datos.getRoles().isEmpty()) {
                throw new IllegalArgumentException("El usuario debe tener al menos un rol asignado");
            }
            existente.getRoles().clear();
            existente.getRoles().addAll(resolverRolesDesdeNombres(datos.getRoles()));
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

        return jwtUtil.generarToken(usuario.getCorreo(),
                usuario.getRoles().stream().map(r -> r.getNombre().name()).toList());
    }
}