package com.example.Eminent.participacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.example.Eminent.eventos.entity.Evento;

/**
 * Entidad que representa la inscripción de un participante en un evento.
 * Una inscripción puede tener estado ACTIVA (confirmada) o EN_ESPERA (lista de espera
 * si el aforo está lleno). También almacena el método de inscripción (formulario o CSV)
 * y los códigos QR asociados para la asistencia.
 */
@Entity
@Table(name = "inscripcion")
@Getter
@Setter
@NoArgsConstructor
public class Inscripcion {

    /** Identificador único autogenerado de la inscripción. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Participante inscrito. Se resuelve con fetch LAZY. No puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id", nullable = false)
    private Participante participante;

    /** Evento en el que está inscrito el participante. Se resuelve con fetch LAZY. No puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    /** Fecha y hora en que se realizó la inscripción. Se establece automáticamente al insertar. */
    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion = LocalDateTime.now();

    /** Métodos disponibles para realizar la inscripción: FORMULARIO (manual) o CSV (masivo). */
    public enum MetodoInscripcion {
        FORMULARIO, CSV
    }

    /** Método utilizado para la inscripción. No puede ser nulo. */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_inscripcion", nullable = false)
    private MetodoInscripcion metodoInscripcion;

    /** Estados posibles de la inscripción: ACTIVA (confirmada, dentro del aforo)
     *  o EN_ESPERA (cola de espera cuando el aforo está lleno). */
    public enum Estado {
        ACTIVA, EN_ESPERA
    }

    /** Estado actual de la inscripción. No puede ser nulo. Por defecto es ACTIVA. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVA;

    /** Código QR corto para identificación rápida del asistente. Es null mientras la inscripción esté EN_ESPERA. */
    @Column(name = "codigo_qr")
    private String codigoQr;

    /** Contenido completo del código QR generado para la verificación de asistencia. */
    @Column(name = "contenido_qr")
    private String contenidoQr;
}