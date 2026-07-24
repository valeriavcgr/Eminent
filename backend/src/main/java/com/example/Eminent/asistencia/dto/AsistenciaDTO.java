package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AsistenciaDTO {

    private Long id;
    private Long inscripcionId;
    private LocalDateTime fechaHora;
    private String metodo;
    private Long registradoPor;

    public AsistenciaDTO() {
    }
}