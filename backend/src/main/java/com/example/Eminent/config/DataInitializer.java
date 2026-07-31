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
        crearOActualizarUsuarioSeed("admin@eminent.com", "Admin1234", Usuario.Rol.ADMIN, "3001234567");
        crearOActualizarUsuarioSeed("operador@eminent.com", "Operador1", Usuario.Rol.OPERADOR, "3002345678");
        crearOActualizarUsuarioSeed("monitor@eminent.com", "Monitor12", Usuario.Rol.MONITOR, "3003456789");
    }

    /**
     * Crea la cuenta seed si no existe, o resincroniza su contraseña si ya existe.
     * Estas 3 cuentas son credenciales de desarrollo conocidas: sin este resync,
     * un cambio de contraseña seed en el código no toma efecto sobre datos ya
     * persistidos en una BD anterior, dejando las credenciales documentadas sin funcionar.
     */
    private void crearOActualizarUsuarioSeed(String correo, String contrasena, Usuario.Rol rol, String telefono) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setNombre(rol.name());
            usuario.setApellido("User");
            usuario.setCorreo(correo);
            usuario.setRol(rol);
            usuario.setTelefono(telefono);
            usuario.setEstado(Usuario.Estado.ACTIVO);
            usuario.setContrasena(passwordEncoder.encode(contrasena));
            usuarioRepository.save(usuario);
            System.out.println("[DataInitializer] Usuario creado: " + correo + " (" + rol + ")");
        } else if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            usuario.setContrasena(passwordEncoder.encode(contrasena));
            usuarioRepository.save(usuario);
            System.out.println("[DataInitializer] Contraseña resincronizada: " + correo);
        }
    }
}