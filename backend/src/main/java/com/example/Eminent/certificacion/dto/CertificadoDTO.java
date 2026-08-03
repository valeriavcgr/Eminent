package com.example.Eminent.certificacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
public class CertificadoDTO {

    private Long id;
    private Long asistenciaId;
    private String codigoUnico;
    private BigDecimal duracionHoras;
    private LocalDateTime fechaEmision;
    private String rutaPdf;
    private String codigoQrVerificacion;

    public CertificadoDTO() {
    }
}