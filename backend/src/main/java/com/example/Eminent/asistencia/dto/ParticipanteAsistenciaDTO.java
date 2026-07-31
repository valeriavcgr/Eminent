package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO que representa el estado de asistencia de un participante individual.
 * Incluye los datos personales del participante y su estado de asistencia
 * al evento, con el método y fecha de registro si aplica.
 */
@Getter
@Setter
public class ParticipanteAsistenciaDTO {

    /** ID de la inscripción del participante. */
    private Long inscripcionId;
    /** Nombre completo del participante. */
    private String participanteNombre;
    /** Documento de identidad del participante. */
    private String participanteDocumento;
    /** Correo electrónico del participante. */
    private String participanteCorreo;
    /** Número de teléfono del participante. */
    private String participanteTelefono;
    /** Indica si el participante asistió al evento. */
    private boolean asistio;
    /** Fecha y hora en que el participante registró su asistencia (null si no asistió). */
    private LocalDateTime fechaHoraAsistencia;
    /** Método de registro de asistencia (MANUAL, QR) o null si no ha registrado asistencia. */
    private String metodo;
}