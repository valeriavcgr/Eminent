package com.example.Eminent.eventos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.example.Eminent.usuarios.entity.Usuario;


@Entity
@Table(name = "evento",
        uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "tipo"}))
@Getter
@Setter
@NoArgsConstructor
public class Evento {

    /** Identificador único autogenerado del evento. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del evento. No puede ser nulo. */
    @Column(nullable = false)
    private String nombre;

    /** Tipos de evento disponibles: TALLER (formación práctica), CAPACITACIÓN (adicional), TORNEO (competitivo). */
    public enum Tipo {
        TALLER, CAPACITACION, TORNEO
    }

    /** Tipo del evento. No puede ser nulo. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    /** Modalidades disponibles: PRESENCIAL (en sede física) o VIRTUAL (en línea). */
    public enum Modalidad {
        PRESENCIAL, VIRTUAL
    }

    /** Modalidad del evento. No puede ser nulo. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    /** Descripción detallada del evento. */
    private String descripcion;

    /** Fecha y hora de inicio del evento. No puede ser nulo. */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    /** Fecha y hora de finalización del evento. No puede ser nulo. */
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    /** Aforo máximo de participantes permitidos en el evento. No puede ser nulo. */
    @Column(nullable = false)
    private Integer aforo;

    public enum Estado {
        PROGRAMADO, EN_CURSO, FINALIZADO, CANCELADO
    }

    /** Estado actual del evento. No puede ser nulo. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.PROGRAMADO;

    /** Usuario que creó el evento. No puede ser nulo. Se resuelve con fetch LAZY.  RECORDAR */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    /** Fecha y hora de creación del registro del evento. Se establece automáticamente al insertar. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}