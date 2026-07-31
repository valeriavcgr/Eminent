package com.example.Eminent.certificacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.Eminent.asistencia.entity.Asistencia;

/**
 * Entidad que representa un certificado de asistencia emitido para un participante.
 * Cada certificado está vinculado exclusivamente a una inscripción de asistencia,
 * contiene un código único de verificación y un PDF generado con datos de duración.
 */
@Entity
@Table(name = "certificado")
@Getter
@Setter
@NoArgsConstructor
public class Certificado {

    /** Identificador único autogenerado del certificado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Inscripción de asistencia vinculada a este certificado. Relación uno a uno, obligatoria y única. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asistencia_id", nullable = false, unique = true)
    private Asistencia asistencia;

    /** Código único generado para identificar el certificado de forma verificable. */
    @Column(name = "codigo_unico", nullable = false, unique = true)
    private String codigoUnico;

    /** Duración total del evento en horas (puede ser decimal, por ejemplo 4.5 para 4 horas y 30 minutos). */
    @Column(name = "duracion_horas", nullable = false)
    private BigDecimal duracionHoras;

    /** Fecha y hora en que se emitió el certificado. Se establece automáticamente al insertar. */
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    /** Ruta de almacenamiento del archivo PDF del certificado. */
    @Column(name = "ruta_pdf", nullable = false)
    private String rutaPdf;

    /** Código QR generado para la verificación en línea de la autenticidad del certificado. */
    @Column(name = "codigo_qr_verificacion", nullable = false)
    private String codigoQrVerificacion;
}