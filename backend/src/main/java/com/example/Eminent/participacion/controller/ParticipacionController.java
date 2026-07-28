package com.example.Eminent.participacion.controller;

import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.entity.Participante;
import com.example.Eminent.participacion.service.ParticipacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inscripciones")
public class ParticipacionController {

    @Autowired private ParticipacionService service;

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
                "codigoQr", resultado.getCodigoQr()
        ));
    }
}