package com.example.Eminent.auditoria.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de auditoría entre capas.
 * Representa un registro de acción realizada en el sistema,
 * incluyendo quién la realizó, qué entidad afectó y la descripción del cambio.
 */
@Getter
@Setter
public class AuditoriaDTO {

    /** Identificador único del registro de auditoría. */
    private Long id;
    /** ID del usuario que realizó la acción (puede ser null para acciones del sistema). */
    private Long usuarioId;
    /** Nombre del usuario (opcional, útil para mostrar directamente en el frontend). */
    private String nombreUsuario;
    /** Acción realizada (CREAR, EDITAR, DESACTIVAR, CANCELAR, VER). */
    private String accion;
    /** Tipo de entidad afectada (USUARIO, EVENTO, PARTICIPANTE, INSCRIPCION, ASISTENCIA, CERTIFICADO). */
    private String tipoAfectado;
    /** ID de la entidad afectada. */
    private Long entidadId;
    /** Descripción detallada de la acción realizada. */
    private String descripcion;
    /** Dirección IP desde la cual se realizó la acción. */
    private String ip;
    /** Fecha y hora en que se registró la acción. */
    private LocalDateTime fechaHora;

    public AuditoriaDTO() {
    }
}