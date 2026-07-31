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

/**
 * Controlador REST para la gestión de certificados de asistencia.
 * Proporciona endpoints para listar eventos finalizados, descargar certificados en PDF
 * y verificar la autenticidad de un certificado mediante su código único.
 */
@RestController
@RequestMapping("/api/certificados")
public class CertificacionController {

    @Autowired private CertificacionService service;

    /**
     * Lista todos los eventos que han finalizado y para los cuales se pueden
     * descargar certificados de asistencia.
     */
    @GetMapping("/eventos-finalizados")
    public ResponseEntity<List<EventoDTO>> eventosFinalizados() {

        return ResponseEntity.ok(service.eventosFinalizados());
    }

    /**
     * Descarga el certificado PDF de un participante para un evento específico,
     * identificado por su número de documento y el ID del evento.
     */
    @GetMapping("/descargar")
    public ResponseEntity<?> descargarPorDocumento(@RequestParam String documento, @RequestParam Long eventoId) {
        Certificado cert = service.buscarPorDocumentoYEvento(documento, eventoId);
        FileSystemResource archivo = new FileSystemResource(cert.getRutaPdf());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificado.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
    }

    /**
     * Verifica la autenticidad de un certificado mediante su código único de verificación.
     * Devuelve los datos del certificado en formato público (sin datos internos).
     */
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