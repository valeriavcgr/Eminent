package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de un participante entre capas.
 * Contiene los datos personales del participante sin exponer la entidad JPA completa.
 */
@Getter
@Setter
public class ParticipanteDTO {

    /** Identificador único del participante. */
    private Long id;
    /** Nombre del participante. */
    private String nombre;
    /** Apellido del participante. */
    private String apellido;
    /** Número de documento de identidad. */
    private String documento;
    /** Correo electrónico del participante. */
    private String correo;
    /** Número de teléfono del participante. */
    private String telefono;
    /** Fecha y hora de creación del registro. */
    private LocalDateTime fechaCreacion;

    public ParticipanteDTO() {
    }
}