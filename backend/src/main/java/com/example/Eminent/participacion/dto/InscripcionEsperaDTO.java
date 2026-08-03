package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO para representar a un participante en la lista de espera de un evento.
 * Incluye información del participante, su posición en la cola y
 * los datos del evento correspondiente.
 */
@Getter
@Setter
public class InscripcionEsperaDTO {

    /** ID de la inscripción en espera. */
    private Long inscripcionId;
    /** Nombre completo del participante en espera. */
    private String participanteNombre;
    /** Documento de identidad del participante. */
    private String participanteDocumento;
    /** Fecha y hora en que se registró la inscripción en espera. */
    private LocalDateTime fechaInscripcion;
    /** Posición actual del participante en la lista de espera. */
    private int posicion;
    /** ID del evento en el que está en espera. */
    private Long eventoId;
    /** Nombre del evento en el que está en espera. */
    private String eventoNombre;
}