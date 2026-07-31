package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de inscripción entre capas.
 * Representa una inscripción confirmada de un participante en un evento,
 * incluyendo el estado, método y códigos QR asociados.
 */
@Getter
@Setter
public class InscripcionDTO {

    /** Identificador único de la inscripción. */
    private Long id;
    /** ID del participante inscrito. */
    private Long participanteId;
    /** ID del evento en el que se inscribió. */
    private Long eventoId;
    /** Fecha y hora en que se realizó la inscripción. */
    private LocalDateTime fechaInscripcion;
    /** Método de inscripción como texto (FORMULARIO o CSV). */
    private String metodoInscripcion;
    /** Código QR corto para identificación rápida del asistente. */
    private String codigoQr;
    /** Estado de la inscripción como texto (ACTIVA o EN_ESPERA). */
    private String estado;

    public InscripcionDTO() {
    }
}