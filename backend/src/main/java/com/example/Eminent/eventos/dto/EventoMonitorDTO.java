package com.example.Eminent.eventos.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class EventoMonitorDTO {

    private Long id;
    private Long eventoId;
    private Long monitorId;
    private String nombreEvento;
    private String nombreMonitor;
    private LocalDateTime fechaAsignacion;

    public EventoMonitorDTO() {
    }
}