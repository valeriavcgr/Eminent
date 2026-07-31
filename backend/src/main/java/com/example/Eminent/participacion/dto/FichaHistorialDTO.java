package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa la ficha histórica completa de un participante.
 * Contiene los datos personales del participante junto con la lista
 * de todas sus inscripciones (incluyendo estado y asistencia).
 */
@Getter
@Setter
public class FichaHistorialDTO {

    /** ID del participante. */
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
    /** Fecha y hora de creación del registro del participante. */
    private LocalDateTime fechaCreacion;
    /** Lista de todas las inscripciones del participante con detalles de evento y asistencia. */
    private List<InscripcionHistorialDTO> inscripciones;

    public FichaHistorialDTO() {
    }
}