package com.example.Eminent.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventoResumenDashboardDTO {
    private Long id;
    private String nombre;
    private String tipo;
    private String modalidad;
    private int aforo;
    private long inscritos;
    private long asistieron;
    private double porcentajeAsistencia;
    private Double promedioSatisfaccion; // null si nadie ha calificado ese evento
}