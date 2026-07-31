package com.example.Eminent.eventos.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de la asignación de un monitor a un evento.
 * Contiene los nombres del evento y del monitor para visualización directa
 * en el frontend sin necesidad de resolverelaciones de entidad.
 */
@Getter
@Setter
public class EventoMonitorDTO {

    /** Identificador único de la asignación. */
    private Long id;
    /** ID del evento asignado. */
    private Long eventoId;
    /** ID del monitor asignado. */
    private Long monitorId;
    /** Nombre del evento para visualización. */
    private String nombreEvento;
    /** Nombre del monitor para visualización. */
    private String nombreMonitor;
    /** Fecha y hora en que se realizó la asignación. */
    private LocalDateTime fechaAsignacion;

    public EventoMonitorDTO() {
    }
}