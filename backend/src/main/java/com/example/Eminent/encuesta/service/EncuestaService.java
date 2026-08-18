package com.example.Eminent.encuesta.service;

import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.certificacion.entity.Certificado;
import com.example.Eminent.certificacion.repository.CertificadoRepository;
import com.example.Eminent.encuesta.dto.EncuestaComentarioDTO;
import com.example.Eminent.encuesta.dto.EncuestaDTO;
import com.example.Eminent.encuesta.entity.Encuesta;
import com.example.Eminent.encuesta.repository.EncuestaRepository;
import com.example.Eminent.participacion.entity.Participante;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EncuestaService {

    @Autowired private CertificadoRepository certificadoRepo;
    @Autowired private EncuestaRepository encuestaRepo;
    @Autowired private AuditoriaService auditoriaService;

    // El rango de calificación (1-5) y el largo máximo del comentario (300) ya los
    // valida EncuestaDTO vía Bean Validation (@Min/@Max/@Size).
    public void registrar(EncuestaDTO datos) {
        Certificado certificado = certificadoRepo.findByCodigoUnico(datos.getCodigoUnico())
                .orElseThrow(() -> new IllegalArgumentException("Certificado no encontrado"));

        Long inscripcionId = certificado.getInscripcion().getId();

        if (encuestaRepo.existsByInscripcionId(inscripcionId)) {
            throw new IllegalArgumentException("Ya has calificado este evento anteriormente");
        }

        Encuesta encuesta = new Encuesta();
        encuesta.setInscripcion(certificado.getInscripcion());
        encuesta.setCalificacion(datos.getCalificacion());
        encuesta.setComentario(datos.getComentario());
        Encuesta guardada = encuestaRepo.save(encuesta);

        auditoriaService.registrar(null, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.ENCUESTA,
                guardada.getId(), "Registro de encuesta de satisfacción (certificado " + datos.getCodigoUnico() + ")", null);
    }

    public Page<EncuestaComentarioDTO> listarPorEvento(Long eventoId, Integer calificacion, Pageable pageable) {
        Page<Encuesta> encuestas = encuestaRepo.buscarPorEvento(eventoId, calificacion, pageable);

        return encuestas.map(e -> {
            Participante participante = e.getInscripcion().getParticipante();
            EncuestaComentarioDTO dto = new EncuestaComentarioDTO();
            dto.setParticipanteNombre(participante.getNombre() + " " + participante.getApellido());
            dto.setCalificacion(e.getCalificacion());
            dto.setComentario(e.getComentario());
            dto.setFechaCreacion(e.getFechaCreacion());
            return dto;
        });
    }

    public Double obtenerPromedioEvento(Long eventoId) {
        return encuestaRepo.promedioPorEvento(eventoId);
    }
}