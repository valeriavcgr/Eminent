package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AsistenciaDTO {

    /** Identificador único del registro de asistencia. */
    private Long id;
    /** ID de la inscripción vinculada a este registro de asistencia. */
    private Long inscripcionId;
    /** Fecha y hora en que se registró la asistencia. */
    private LocalDateTime fechaHora;
    /** Método utilizado para registrar la asistencia (MANUAL, QR, etc.). */
    private String metodo;
    /** ID del usuario que registró la asistencia. */
    private Long registradoPor;

    public AsistenciaDTO() {
    }
}