package com.example.Eminent.usuarios.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String rol;
    private String estado;
    private LocalDateTime fechaCreacion;

    public UsuarioDTO() {
    }
}