package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO que representa una fila del archivo CSV durante el proceso de importación.
 * Contiene los datos del participante extraídos de la fila y el resultado
 * de la validación (si es válida o no y la razón en caso de error).
 */
@Getter
@Setter
public class FilaCsvDTO {

    /** Número de línea en el archivo CSV (comenzando en 1). */
    private int numeroFila;
    /** Nombre del participante extraído de la fila. */
    private String nombre;
    /** Apellido del participante extraído de la fila. */
    private String apellido;
    /** Número de documento de identidad extraído de la fila. */
    private String documento;
    /** Correo electrónico extraído de la fila. */
    private String correo;
    /** Número de teléfono extraído de la fila. */
    private String telefono;
    /** Indica si la fila pasó todas las validaciones y se puede importar. */
    private boolean valida;
    /** Motivo del error de validación si la fila no es válida (null si valida es true). */
    private String motivoError;
}