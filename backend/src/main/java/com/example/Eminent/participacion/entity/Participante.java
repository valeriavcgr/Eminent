package com.example.Eminent.participacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que representa a un participante inscrito en eventos del sistema Eminent.
 * Almacena los datos personales del participante (nombre, apellido, documento, correo, teléfono).
 * El documento es único para evitar inscripciones duplicadas.
 */
@Entity
@Table(name = "participante")
@Getter
@Setter
@NoArgsConstructor
public class Participante {

    /** Identificador único autogenerado del participante. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del participante. No puede ser nulo. */
    @Column(nullable = false)
    private String nombre;

    /** Apellido del participante. No puede ser nulo. */
    @Column(nullable = false)
    private String apellido;

    /** Documento de identidad del participante. No puede ser nulo ni duplicado. */
    @Column(nullable = false, unique = true)
    private String documento;

    /** Correo electrónico del participante. */
    private String correo;

    /** Número de teléfono del participante. Debe coincidir con el formato +[0-9]{7,15}. */
    private String telefono;

    /** Fecha y hora de creación del registro del participante. Se establece automáticamente al insertar. */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}