package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ParticipanteAsistenciaDTO {
    private Long inscripcionId;
    private String participanteNombre;
    private String participanteDocumento;
    private String participanteCorreo;
    private String participanteTelefono;
    private boolean asistio;
    private LocalDateTime fechaHoraAsistencia;
    private String metodo; // MANUAL, QR, o null si no ha asistido
}