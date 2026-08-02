package com.example.Eminent.participacion.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilaCsvDTO {
    private int numeroFila;
    private String nombre;
    private String apellido;
    private String documento;
    private String correo;
    private String telefono;
    private boolean valida;
    private String motivoError;
    /** true si esta fila, al confirmar, quedará en estado EN_ESPERA por falta de cupo. */
    private boolean iraListaEspera;
}