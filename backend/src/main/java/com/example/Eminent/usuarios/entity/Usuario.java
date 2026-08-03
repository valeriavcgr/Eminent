package com.example.Eminent.usuarios.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    /** Identificador único autogenerado del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del usuario. No puede ser nulo. */
    @Column(nullable = false)
    private String nombre;

    /** Apellido del usuario. No puede ser nulo. */
    @Column(nullable = false)
    private String apellido;

    /** Correo electrónico único del usuario. Usado como identificador de autenticación. */
    @Column(nullable = false, unique = true)
    private String correo;

    /** Contraseña encriptada del usuario. No se almacena en texto plano. */
    @Column(nullable = false)
    private String contrasena;

    /** Número de teléfono opcional del usuario. Debe coincidir con el formato +[0-9]{7,15}. */
    private String telefono;

    /** Roles disponibles en el sistema: ADMIN (control total), OPERADOR (gestión de eventos), MONITOR (vista de monitoreo). */
    public enum Rol {
        ADMIN, OPERADOR, MONITOR
    }

    /** Rol asignado al usuario. No puede ser nulo. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    /** Estados posibles del usuario: ACTIVO (puede iniciar sesión) o INACTIVO (bloqueado). */
    public enum Estado {
        ACTIVO, INACTIVO
    }

    /** Estado actual del usuario. Por defecto es ACTIVO. No puede ser nulo. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVO;

    /** Fecha y hora de creación del registro. Se establece automáticamente al insertar. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}