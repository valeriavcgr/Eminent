package com.example.Eminent.encuesta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.example.Eminent.participacion.entity.Inscripcion;

@Entity
@Table(name = "encuesta")
@Getter
@Setter
@NoArgsConstructor
public class Encuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id", nullable = false, unique = true)
    private Inscripcion inscripcion;

    @Column(nullable = false)
    private int calificacion; // 1 a 5

    @Column(columnDefinition = "TEXT")
    private String comentario; // opcional

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}