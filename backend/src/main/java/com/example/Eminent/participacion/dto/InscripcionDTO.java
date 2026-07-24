package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class InscripcionDTO {

    private Long id;
    private Long participanteId;
    private Long eventoId;
    private LocalDateTime fechaInscripcion;
    private String metodoInscripcion;
    private String codigoQr;
    private String estado;

    public InscripcionDTO() {
    }
}