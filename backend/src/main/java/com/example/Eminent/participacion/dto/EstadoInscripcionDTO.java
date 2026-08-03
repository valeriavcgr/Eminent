package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadoInscripcionDTO {
    private String estado; // ACTIVA o EN_ESPERA
    private String codigoQr; // solo si estado = ACTIVA
    private Integer posicion; // solo si estado = EN_ESPERA
}