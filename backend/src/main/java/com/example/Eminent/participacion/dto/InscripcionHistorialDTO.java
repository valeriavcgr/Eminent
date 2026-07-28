package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class InscripcionHistorialDTO {

    private Long eventoId;
    private String eventoNombre;
    private String eventoTipo;
    private String eventoEstado;
    private String inscripcionEstado;
    private LocalDateTime fechaInscripcion;
    private boolean asistio;
    private LocalDateTime fechaAsistencia;
    private String metodoAsistencia;
    private String codigoCertificado;

    public InscripcionHistorialDTO() {
    }
}