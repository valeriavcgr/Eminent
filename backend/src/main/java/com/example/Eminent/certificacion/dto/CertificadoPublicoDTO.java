package com.example.Eminent.certificacion.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CertificadoPublicoDTO {
    private String participanteNombre;
    private String eventoNombre;
    private BigDecimal duracionHoras;
    private String fechasEvento;
    private String fechaEmision;
    private String codigoUnico;
    private boolean valido;
}