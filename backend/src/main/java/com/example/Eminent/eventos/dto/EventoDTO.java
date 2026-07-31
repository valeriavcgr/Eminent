package com.example.Eminent.eventos.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de un evento entre capas.
 * Incluye información del evento (nombre, tipo, modalidad, fechas, aforo)
 * junto con datos agregados como número de inscritos y estado como texto.
 */
@Getter
@Setter
public class EventoDTO {

    /** Identificador único del evento. */
    private Long id;
    /** Nombre del evento. */
    private String nombre;
    /** Tipo del evento como texto (TALLER, CAPACITACION, TORNEO). */
    private String tipo;
    /** Modalidad del evento como texto (PRESENCIAL o VIRTUAL). */
    private String modalidad;
    /** Descripción del evento. */
    private String descripcion;
    /** Fecha y hora de inicio del evento. */
    private LocalDateTime fechaInicio;
    /** Fecha y hora de finalización del evento. */
    private LocalDateTime fechaFin;
    /** Aforo máximo de participantes permitidos. */
    private Integer aforo;
    /** Número actual de inscritos en el evento. */
    private Long inscritos;
    /** Estado del evento como texto (PROGRAMADO, EN_CURSO, FINALIZADO, CANCELADO). */
    private String estado;
    /** ID del usuario que creó el evento. */
    private Long creadoPor;
    /** Fecha y hora de creación del evento. */
    private LocalDateTime fechaCreacion;

    public EventoDTO() {
    }
}