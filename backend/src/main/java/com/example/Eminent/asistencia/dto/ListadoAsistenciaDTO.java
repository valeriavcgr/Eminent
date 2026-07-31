package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * DTO que agrupa un resumen de asistencia con la lista detallada de participantes.
 * Se utiliza para devolver la información completa de la lista de asistencia de un evento.
 */
@Getter
@Setter
public class ListadoAsistenciaDTO {

    /** Resumen estadístico de la asistencia del evento. */
    private ResumenAsistenciaDTO resumen;
    /** Lista detallada de participantes con su estado de asistencia. */
    private List<ParticipanteAsistenciaDTO> participantes;
}