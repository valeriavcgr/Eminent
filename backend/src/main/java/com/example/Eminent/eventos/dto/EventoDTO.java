package com.example.Eminent.eventos.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Se reutiliza tanto para crear (todo obligatorio) como para editar (actualización
 * parcial). Igual que en UsuarioRequest, las anotaciones son solo de formato/rango
 * para no romper la edición parcial; la obligatoriedad al crear, el aforo vs.
 * inscritos y la validación de jornadas siguen en EventosService.
 *
 * fechaInicio/fechaFin son de solo lectura: se calculan a partir de las jornadas
 * (min/max de sus horarios) y se ignoran si vienen en la request de creación/edición.
 */
@Getter
@Setter
public class EventoDTO {

    private Long id;

    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;

    private String tipo;
    private String modalidad;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    @Positive(message = "El aforo debe ser un número entero mayor a cero")
    private Integer aforo;

    private Long inscritos;
    private String estado;
    private Long creadoPor;
    private LocalDateTime fechaCreacion;

    /** Jornadas (días) del evento. Obligatorio al menos una al crear. */
    private List<EventoDiaDTO> jornadas;

    public EventoDTO() {
    }
}