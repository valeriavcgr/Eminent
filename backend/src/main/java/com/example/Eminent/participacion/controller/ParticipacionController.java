package com.example.Eminent.participacion.controller;

import com.example.Eminent.participacion.dto.InscripcionDTO;
import com.example.Eminent.participacion.dto.InscripcionDTO;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.entity.Participante;
import com.example.Eminent.participacion.service.ParticipacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inscripciones")
public class ParticipacionController {

    @Autowired private ParticipacionService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<?> listarPorEvento(@RequestParam Long eventoId) {
        List<Inscripcion> lista = service.listarInscripcionesPorEvento(eventoId);
        return ResponseEntity.ok(lista.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    private InscripcionDTO toDTO(Inscripcion inscripcion) {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setId(inscripcion.getId());
        dto.setParticipanteId(inscripcion.getParticipante().getId());
        dto.setEventoId(inscripcion.getEvento().getId());
        dto.setFechaInscripcion(inscripcion.getFechaInscripcion());
        dto.setMetodoInscripcion(inscripcion.getMetodoInscripcion().name());
        dto.setCodigoQr(inscripcion.getCodigoQr());
        dto.setEstado(inscripcion.getEstado().name());
        return dto;
    }

    @PostMapping
    public ResponseEntity<?> inscribir(@RequestBody Map<String, Object> body) throws Exception {
        Participante p = new Participante();
        p.setNombre((String) body.get("nombre"));
        p.setApellido((String) body.get("apellido"));
        p.setDocumento((String) body.get("documento"));
        p.setCorreo((String) body.get("correo"));
        p.setTelefono((String) body.get("telefono"));

        Long eventoId = Long.valueOf(body.get("eventoId").toString());
        Inscripcion resultado = service.inscribir(p, eventoId);

        if (resultado.getEstado() == Inscripcion.Estado.EN_ESPERA) {
            long posicion = service.posicionEnCola(resultado.getId(), eventoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "estado", "EN_ESPERA",
                    "mensaje", "Estás en lista de espera",
                    "posicion", posicion
            ));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "estado", "ACTIVA",
                "mensaje", "Inscripción exitosa",
                "codigoQr", resultado.getContenidoQr()
        ));
    }
}