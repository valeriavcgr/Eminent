package com.example.Eminent.eventos.entity;

import com.example.Eminent.usuarios.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad de relación muchos a muchos entre eventos y monitores.
 * Asigna un usuario con rol MONITOR para supervisionar un evento específico.
 * La combinación de evento y monitor debe ser única.
 */
@Entity
@Table(name = "evento_monitor",
        uniqueConstraints = @UniqueConstraint(columnNames = {"evento_id", "monitor_id"}))
@Getter
@Setter
@NoArgsConstructor
public class EventoMonitor {

    /** Identificador único autogenerado de la asignación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Evento al que está asignado el monitor. Se resuelve con fetch LAZY. No puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    /** Usuario con rol MONITOR asignado al evento. Se resuelve con fetch LAZY. No puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Usuario monitor;

    /** Fecha y hora en que se asignó el monitor al evento. Se establece automáticamente al insertar. */
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion = LocalDateTime.now();
}