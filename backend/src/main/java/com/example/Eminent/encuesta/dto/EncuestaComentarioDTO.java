package com.example.Eminent.encuesta.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class EncuestaComentarioDTO {
    private String participanteNombre;
    private int calificacion;
    private String comentario;
    private LocalDateTime fechaCreacion;
}