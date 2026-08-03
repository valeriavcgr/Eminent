package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO que representa una inscripción en el historial detallado de un participante.
 * Muestra información completa del evento y del estado de la inscripción,
 * incluyendo asistencia y certificado si aplica.
 */
@Getter
@Setter
public class InscripcionHistorialDTO {

    /** ID del evento al que está inscrito. */
    private Long eventoId;
    /** Nombre del evento. */
    private String eventoNombre;
    /** Tipo de evento como texto (TALLER, CAPACITACION, TORNEO). */
    private String eventoTipo;
    /** Estado del evento como texto (PROGRAMADO, EN_CURSO, FINALIZADO, CANCELADO). */
    private String eventoEstado;
    /** Estado de la inscripción como texto (ACTIVA o EN_ESPERA). */
    private String inscripcionEstado;
    /** Fecha y hora de la inscripción. */
    private LocalDateTime fechaInscripcion;
    /** Indica si el participante asistió al evento. */
    private boolean asistio;
    /** Fecha y hora en que el participante registró su asistencia (null si no asistió). */
    private LocalDateTime fechaAsistencia;
    /** Método mediante el cual se registró la asistencia. */
    private String metodoAsistencia;
    /** Código del certificado generado, si aplica. */
    private String codigoCertificado;

    public InscripcionHistorialDTO() {
    }
}