package com.example.Eminent.eventos.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
public class EventoDTO {

    private Long id;
    private String nombre;
    private String tipo;
    private String modalidad;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer aforo;
    private Long inscritos;
    private String estado;
    private Long creadoPor;
    private LocalDateTime fechaCreacion;

    public EventoDTO() {
    }
}