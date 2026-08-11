package com.example.Eminent.encuesta.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncuestaDTO {
    @NotBlank(message = "El código del certificado es obligatorio")
    private String codigoUnico;

    @Min(value = 1, message = "La calificación debe estar entre 1 y 5 estrellas")
    @Max(value = 5, message = "La calificación debe estar entre 1 y 5 estrellas")
    private int calificacion;

    @Size(max = 300, message = "El comentario no puede superar los 300 caracteres")
    private String comentario;
}