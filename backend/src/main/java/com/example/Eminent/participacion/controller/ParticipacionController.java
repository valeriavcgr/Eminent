package com.example.Eminent.participacion.controller;

import com.example.Eminent.participacion.dto.InscripcionDTO;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.service.ParticipacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inscripciones")
public class ParticipacionController {

    @Autowired private ParticipacionService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<Page<InscripcionDTO>> listarPorEvento(@RequestParam Long eventoId, Pageable pageable) {
        Page<Inscripcion> pagina = service.listarInscripcionesPorEvento(eventoId, pageable);
        return ResponseEntity.ok(pagina.map(this::toDTO));
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
        com.example.Eminent.participacion.entity.Participante p = new com.example.Eminent.participacion.entity.Participante();
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

    @GetMapping("/consultar")
    public ResponseEntity<?> consultarEstado(@RequestParam String documento, @RequestParam Long eventoId) {
        return ResponseEntity.ok(service.consultarEstado(documento, eventoId));
    }
}