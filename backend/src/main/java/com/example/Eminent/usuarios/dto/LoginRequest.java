package com.example.Eminent.usuarios.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO para recibir las credenciales de inicio de sesión del usuario.
 * Contiene el correo y la contraseña proporcionados en el formulario de login.
 */
@Getter
@Setter
public class LoginRequest {
    /** Correo electrónico del usuario para autenticación. */
    private String correo;
    /** Contraseña del usuario (será encriptada y comparada en el servicio). */
    private String contrasena;
}