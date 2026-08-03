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

    /** Identificador único autogenerado del registro de auditoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que realizó la acción. Puede ser null para acciones automáticas del sistema.
     *  Se resuelve con fetch LAZY. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    /** Tipos de acciones que se registran en el auditoría: CREAR, EDITAR, DESACTIVAR, CANCELAR, VER. */
    public enum Accion {
        CREAR, EDITAR, DESACTIVAR, CANCELAR, VER
    }

    /** Acción que se realizó sobre la entidad afectada. No puede ser nulo. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Accion accion;

    /** Tipos de entidades que pueden ser afectadas por una acción: USUARIO, EVENTO, PARTICIPANTE,
     *  INSCRIPCION, ASISTENCIA, CERTIFICADO. */
    public enum TipoAfectado {
        USUARIO, EVENTO, PARTICIPANTE, INSCRIPCION, ASISTENCIA, CERTIFICADO
    }

    /** Tipo de entidad a la que se aplicó la acción. No puede ser nulo. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_afectado", nullable = false)
    private TipoAfectado tipoAfectado;

    /** Identificador numérico de la entidad afectada (por ejemplo, el ID del usuario, evento o inscripción). */
    @Column(name = "entidad_id")
    private Long entidadId;

    /** Descripción detallada de la acción realizada. No puede ser nulo. */
    @Column(nullable = false)
    private String descripcion;

    /** Dirección IP desde la cual se realizó la acción. */
    private String ip;

    /** Fecha y hora en que se registró la acción. Se establece automáticamente al insertar. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();
}