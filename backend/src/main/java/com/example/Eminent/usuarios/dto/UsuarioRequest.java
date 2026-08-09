package com.example.Eminent.usuarios.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO de entrada para crear/editar un usuario. Recibe los roles como nombres de texto
 * (ADMIN, OPERADOR, MONITOR) en vez de la colección de entidades {@code Rol}.
 */
@Getter
@Setter
public class UsuarioRequest {

    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private String telefono;
    private List<String> roles;
}
