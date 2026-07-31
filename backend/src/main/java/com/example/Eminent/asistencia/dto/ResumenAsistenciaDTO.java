package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO que contiene el resumen de asistencia de un evento.
 * Muestra el total de inscritos, cuántos asistieron y el porcentaje de ocupación del aforo.
 */
@Getter
@Setter
public class ResumenAsistenciaDTO {

    /** Total de participantes inscritos en el evento. */
    private long totalInscritos;
    /** Total de participantes que registraron su asistencia. */
    private long totalAsistieron;
    /** Porcentaje del aforo que está ocupado (asistieron / aforo). */
    private double porcentajeAforoOcupado;
}
