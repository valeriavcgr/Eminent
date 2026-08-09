package com.example.Eminent.usuarios.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


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

    /**
     * Columna legada de un único rol por usuario, previa a la migración a roles múltiples.
     * Se conserva solo para que {@code DataInitializer} pueda leerla y migrar su valor a
     * {@link #roles} en el arranque; ya no se usa en ninguna lógica de negocio.
     */
    @Deprecated
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = true)
    private Rol rolLegado;

    /** Roles asignados al usuario (relación muchos-a-muchos vía tabla usuario_roles). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<com.example.Eminent.usuarios.entity.Rol> roles = new HashSet<>();

    /** Indica si el usuario tiene el rol dado entre sus roles asignados. */
    public boolean tieneRol(Rol nombre) {
        return roles.stream().anyMatch(r -> r.getNombre() == nombre);
    }

    /** Rol de mayor rango entre los asignados al usuario, según el orden ADMIN &gt; OPERADOR &gt; MONITOR. Null si no tiene roles. */
    public Rol rolDeMayorRango() {
        for (Rol candidato : new Rol[]{Rol.ADMIN, Rol.OPERADOR, Rol.MONITOR}) {
            if (tieneRol(candidato)) return candidato;
        }
        return null;
    }

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