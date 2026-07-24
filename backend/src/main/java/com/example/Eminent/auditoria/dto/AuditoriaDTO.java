package com.example.Eminent.auditoria.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AuditoriaDTO {

    private Long id;
    private Long usuarioId;
    private String nombreUsuario; // opcional, útil para mostrar directo en el frontend
    private String accion;
    private String tipoAfectado;
    private Long entidadId;
    private String descripcion;
    private String ip;
    private LocalDateTime fechaHora;

    public AuditoriaDTO() {
    }
}