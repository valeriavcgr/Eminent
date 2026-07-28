package com.example.Eminent.participacion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.example.Eminent.eventos.entity.Evento;

@Entity
@Table(name = "inscripcion")
@Getter
@Setter
@NoArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id", nullable = false)
    private Participante participante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion = LocalDateTime.now();

    public enum MetodoInscripcion {
        FORMULARIO, CSV
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_inscripcion", nullable = false)
    private MetodoInscripcion metodoInscripcion;

    public enum Estado {
        ACTIVA, EN_ESPERA
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.ACTIVA;

    @Column(name = "codigo_qr")
    private String codigoQr; // null mientras esté EN_ESPERA

    @Column(name = "contenido_qr")
    private String contenidoQr;
}