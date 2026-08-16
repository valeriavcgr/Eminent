package com.example.Eminent.asistencia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.example.Eminent.eventos.entity.EventoDia;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.usuarios.entity.Usuario;

@Entity
@Table(name = "asistencia",
        uniqueConstraints = @UniqueConstraint(columnNames = {"inscripcion_id", "evento_dia_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Una inscripción puede tener varias asistencias: una por jornada del evento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    /** Jornada del evento a la que corresponde este registro de asistencia. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_dia_id", nullable = false)
    private EventoDia eventoDia;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    public enum Metodo {
        MANUAL, QR
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Metodo metodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor;
}