package com.example.Eminent.eventos.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class EventoDiaDTO {

    private Long id;
    private Integer numeroDia;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public EventoDiaDTO() {
    }

    public EventoDiaDTO(Long id, Integer numeroDia, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        this.id = id;
        this.numeroDia = numeroDia;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }
}
