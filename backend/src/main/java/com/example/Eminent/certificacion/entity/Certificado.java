package com.example.Eminent.certificacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.Eminent.asistencia.entity.Asistencia;

@Entity
@Table(name = "certificado")
@Getter
@Setter
@NoArgsConstructor
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asistencia_id", nullable = false, unique = true)
    private Asistencia asistencia;

    @Column(name = "codigo_unico", nullable = false, unique = true)
    private String codigoUnico;

    @Column(name = "duracion_horas", nullable = false)
    private BigDecimal duracionHoras;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "ruta_pdf", nullable = false)
    private String rutaPdf;

    @Column(name = "codigo_qr_verificacion", nullable = false)
    private String codigoQrVerificacion;
}