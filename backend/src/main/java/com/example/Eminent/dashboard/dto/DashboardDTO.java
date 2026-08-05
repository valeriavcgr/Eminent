package com.example.Eminent.dashboard.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;


@Getter
@Setter
public class DashboardDTO {

    private long totalEventos;
    private Map<String, Long> eventosPorTipo;
    private Map<String, Long> eventosPorModalidad;
    private Map<String, Long> eventosPorEstado;
    private int aforoTotal;
    private long inscritosActivosTotal;
    private long asistieronTotal;
    private long asistieronQrTotal;
    private long asistieronManualTotal;
    private double porcentajeAforoOcupado;
    private double porcentajeAsistenciaSobreInscritos;
    private java.util.List<EventoResumenDashboardDTO> detalleEventos;
    private Double promedioSatisfaccionGeneral;
}