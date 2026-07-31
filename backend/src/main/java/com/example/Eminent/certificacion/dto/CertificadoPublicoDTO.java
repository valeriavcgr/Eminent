package com.example.Eminent.certificacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * DTO público para mostrar la verificación de un certificado.
 * Se expone al frontend para validar la autenticidad de un certificado
 * mediante su código único, sin revelar datos internos del sistema.
 */
@Getter
@Setter
public class CertificadoPublicoDTO {

    /** Nombre completo del participante al que se emitió el certificado. */
    private String participanteNombre;
    /** Nombre del evento para el cual se emitió el certificado. */
    private String eventoNombre;
    /** Duración del evento en horas. */
    private BigDecimal duracionHoras;
    /** Fecha de emisión del certificado como texto formateado. */
    private String fechaEmision;
    /** Código único de verificación del certificado. */
    private String codigoUnico;
    /** Indica si el certificado es válido (true) o no ha sido encontrado (false). */
    private boolean valido;
}