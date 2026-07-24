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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    public enum Tipo {
        TALLER, CAPACITACION, TORNEO
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    public enum Modalidad {
        PRESENCIAL, VIRTUAL
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private Integer aforo;

    public enum Estado {
        PROGRAMADO, EN_CURSO, FINALIZADO, CANCELADO
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.PROGRAMADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}