package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumenAsistenciaDTO {
    private long totalInscritos;
    private long totalAsistieron;
    private double porcentajeAforoOcupado;
}
