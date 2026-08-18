package com.example.Eminent.asistencia.dto;

import com.example.Eminent.eventos.dto.EventoDiaDTO;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ListadoAsistenciaDTO {
    private ResumenAsistenciaDTO resumen;
    private List<ParticipanteAsistenciaDTO> participantes;
    private String eventoNombre;
    private String eventoEstado;
    /** Total de jornadas que tiene el evento. */
    private int totalJornadas;
    /** Jornada activa en este momento, o null si no hay ninguna en curso. */
    private EventoDiaDTO jornadaActiva;
}
