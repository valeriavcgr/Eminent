package com.example.Eminent.auditoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.example.Eminent.usuarios.entity.Usuario;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true) // null = acción automática del Sistema
    private Usuario usuario;

    public enum Accion {
        CREAR, EDITAR, DESACTIVAR, CANCELAR, VER
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Accion accion;

    public enum TipoAfectado {
        USUARIO, EVENTO, PARTICIPANTE, INSCRIPCION, ASISTENCIA, CERTIFICADO
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_afectado", nullable = false)
    private TipoAfectado tipoAfectado;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(nullable = false)
    private String descripcion;

    private String ip;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();
}