package com.example.Eminent.asistencia.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ListadoAsistenciaDTO {
    private ResumenAsistenciaDTO resumen;
    private List<ParticipanteAsistenciaDTO> participantes;
}