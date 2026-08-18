package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;

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
    /** Cantidad de jornadas del evento a las que asistió. */
    private int diasAsistidos;
    /** Indica si asistió a todas las jornadas del evento (requisito para certificarse). */
    private boolean asistioCompleto;
    /** Indica si ya registró asistencia en la jornada actualmente activa. */
    private boolean asistioJornadaActiva;
}
