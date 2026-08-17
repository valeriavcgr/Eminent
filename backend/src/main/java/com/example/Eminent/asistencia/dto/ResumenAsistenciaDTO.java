package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ResumenAsistenciaDTO {

    /** Total de participantes inscritos en el evento. */
    private long totalInscritos;
    /** Total de participantes que registraron su asistencia. */
    private long totalAsistieron;
    /** Porcentaje de inscritos que asistió (asistieron / inscritos). */
    private double porcentajeAsistencia;
}
