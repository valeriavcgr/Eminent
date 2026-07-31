package com.example.Eminent.config;

import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearUsuarioSiNoExiste("admin@eminent.com", "Admin1234", Usuario.Rol.ADMIN, "3001234567", "Activo");
        crearUsuarioSiNoExiste("operador@eminent.com", "Operador1", Usuario.Rol.OPERADOR, "3002345678", "Activo");
        crearUsuarioSiNoExiste("monitor@eminent.com", "Monitor12", Usuario.Rol.MONITOR, "3003456789", "Activo");
    }

    private void crearUsuarioSiNoExiste(String correo, String contrasena, Usuario.Rol rol, String telefono, String estado) {
        if (!usuarioRepository.existsByCorreo(correo)) {
            Usuario usuario = new Usuario();
            usuario.setNombre(rol.name());
            usuario.setApellido("User");
            usuario.setCorreo(correo);
            usuario.setContrasena(passwordEncoder.encode(contrasena));
            usuario.setRol(rol);
            usuario.setTelefono(telefono);
            usuario.setEstado(Usuario.Estado.ACTIVO);
            usuarioRepository.save(usuario);
            System.out.println("[DataInitializer] Usuario creado: " + correo + " (" + rol + ")");
        } else {
            System.out.println("[DataInitializer] Usuario ya existe: " + correo);
        }
    }
}