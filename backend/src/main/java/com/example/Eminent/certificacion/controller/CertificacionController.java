package com.example.Eminent.certificacion.controller;

import com.example.Eminent.certificacion.dto.CertificadoPublicoDTO;
import com.example.Eminent.certificacion.entity.Certificado;
import com.example.Eminent.certificacion.service.CertificacionService;
import com.example.Eminent.eventos.entity.Evento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Eminent.eventos.dto.EventoDTO;

import java.util.List;

@RestController
@RequestMapping("/api/certificados")
public class CertificacionController {

    @Autowired private CertificacionService service;

    @GetMapping("/eventos-finalizados")
    public ResponseEntity<List<EventoDTO>> eventosFinalizados() {

        return ResponseEntity.ok(service.eventosFinalizados());
    }

    @GetMapping("/descargar")
    public ResponseEntity<?> descargarPorDocumento(@RequestParam String documento, @RequestParam Long eventoId) {
        Certificado cert = service.buscarPorDocumentoYEvento(documento, eventoId);
        FileSystemResource archivo = new FileSystemResource(cert.getRutaPdf());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificado.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
    }

    @GetMapping("/verificar/{codigo}")
    public ResponseEntity<CertificadoPublicoDTO> verificar(@PathVariable String codigo) {
        Certificado cert = service.verificarPorCodigo(codigo);
        CertificadoPublicoDTO dto = new CertificadoPublicoDTO();
        dto.setParticipanteNombre(cert.getAsistencia().getInscripcion().getParticipante().getNombre()
                + " " + cert.getAsistencia().getInscripcion().getParticipante().getApellido());
        dto.setEventoNombre(cert.getAsistencia().getInscripcion().getEvento().getNombre());
        dto.setDuracionHoras(cert.getDuracionHoras());
        dto.setFechaEmision(cert.getFechaEmision().toString());
        dto.setCodigoUnico(cert.getCodigoUnico());
        dto.setValido(true);
        return ResponseEntity.ok(dto);
    }
}