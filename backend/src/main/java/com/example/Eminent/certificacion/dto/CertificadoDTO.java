package com.example.Eminent.certificacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de un certificado entre capas.
 * Contiene toda la información relevante del certificado emitido,
 * incluyendo la duración del evento, el código único y la ruta del PDF.
 */
@Getter
@Setter
public class CertificadoDTO {

    /** Identificador único del certificado. */
    private Long id;
    /** ID de la inscripción de asistencia vinculada a este certificado. */
    private Long asistenciaId;
    /** Código único generado para verificar la autenticidad del certificado. */
    private String codigoUnico;
    /** Duración total del evento en horas. */
    private BigDecimal duracionHoras;
    /** Fecha y hora en que se emitió el certificado. */
    private LocalDateTime fechaEmision;
    /** Ruta de almacenamiento del archivo PDF del certificado. */
    private String rutaPdf;
    /** Código QR generado para la verificación en línea del certificado. */
    private String codigoQrVerificacion;

    public CertificadoDTO() {
    }
}