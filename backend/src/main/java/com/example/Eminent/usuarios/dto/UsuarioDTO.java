package com.example.Eminent.usuarios.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de usuario entre capas sin exponer la entidad completa.
 * Contiene los campos esenciales para la visualización y manipulación
 * de usuarios en la API.
 */
@Getter
@Setter
public class UsuarioDTO {

    /** Identificador único del usuario. */
    private Long id;
    /** Nombre del usuario. */
    private String nombre;
    /** Apellido del usuario. */
    private String apellido;
    /** Correo electrónico del usuario. Usado como identificador de autenticación. */
    private String correo;
    /** Número de teléfono del usuario. */
    private String telefono;
    /** Rol asignado del usuario como texto (ADMIN, OPERADOR, MONITOR). */
    private String rol;
    /** Estado actual del usuario como texto (ACTIVO o INACTIVO). */
    private String estado;
    /** Fecha y hora de creación del registro. */
    private LocalDateTime fechaCreacion;

    public UsuarioDTO() {
    }
}