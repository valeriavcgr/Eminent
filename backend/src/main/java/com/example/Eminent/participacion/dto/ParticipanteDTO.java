package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ParticipanteDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String documento;
    private String correo;
    private String telefono;
    private LocalDateTime fechaCreacion;

    public ParticipanteDTO() {
    }
}