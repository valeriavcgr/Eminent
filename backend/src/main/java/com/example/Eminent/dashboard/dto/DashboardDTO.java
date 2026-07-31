package com.example.Eminent.dashboard.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

/**
 * DTO para transferir datos del dashboard principal.
 * Contiene estadísticas consolidadas de eventos, inscripciones y asistencias
 * para la visualización en la página de resumen del sistema.
 */
@Getter
@Setter
public class DashboardDTO {

    /** Total de eventos registrados en el sistema. */
    private long totalEventos;
    /** Distribución de eventos por tipo (TALLER, CAPACITACION, TORNEO). */
    private Map<String, Long> eventosPorTipo;
    /** Distribución de eventos por modalidad (PRESENCIAL, VIRTUAL). */
    private Map<String, Long> eventosPorModalidad;
    /** Distribución de eventos por estado (PROGRAMADO, EN_CURSO, FINALIZADO, CANCELADO). */
    private Map<String, Long> eventosPorEstado;
    /** Suma total del aforo de todos los eventos. */
    private int aforoTotal;
    /** Total de inscritos activos en todos los eventos. */
    private long inscritosActivosTotal;
    /** Total de participantes que ya registraron asistencia. */
    private long asistieronTotal;
    /** Porcentaje de aforo ocupado respecto al total disponible. */
    private double porcentajeAforoOcupado;
    /** Porcentaje de asistencia sobre el total de inscritos. */
    private double porcentajeAsistenciaSobreInscritos;
}