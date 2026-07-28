package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class InscripcionEsperaDTO {
    private Long inscripcionId;
    private String participanteNombre;
    private String participanteDocumento;
    private LocalDateTime fechaInscripcion;
    private int posicion;
}