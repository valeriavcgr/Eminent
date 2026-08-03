package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class FichaHistorialDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String documento;
    private String correo;
    private String telefono;
    private LocalDateTime fechaCreacion;
    private List<InscripcionHistorialDTO> inscripciones;

    public FichaHistorialDTO() {
    }
}