package com.example.Eminent.eventos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

/** Representa una jornada (día) dentro de un evento que puede durar uno o varios días. */
@Entity
@Table(name = "evento_dia",
        uniqueConstraints = @UniqueConstraint(columnNames = {"evento_id", "fecha"}))
@Getter
@Setter
@NoArgsConstructor
public class EventoDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    /** Orden de la jornada dentro del evento (1..N), para mostrar "Día 2 de 3". */
    @Column(name = "numero_dia", nullable = false)
    private Integer numeroDia;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;
}
