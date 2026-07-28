package com.example.Eminent.participacion.service;

import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.entity.Participante;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.participacion.repository.ParticipanteRepository;
import com.example.Eminent.participacion.dto.InscripcionEsperaDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipacionService {

    @Autowired private ParticipanteRepository participanteRepo;
    @Autowired private InscripcionRepository inscripcionRepo;
    @Autowired private EventoRepository eventoRepo;
    @Autowired private QrService qrService;
    @Autowired private AuditoriaService auditoriaService;

    public Inscripcion inscribir(Participante datosParticipante, Long eventoId) throws Exception {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (evento.getEstado() != Evento.Estado.PROGRAMADO) {
            throw new IllegalArgumentException("Este evento ya no admite inscripciones");
        }

        Participante participante = participanteRepo.findByDocumento(datosParticipante.getDocumento())
                .orElseGet(() -> participanteRepo.save(datosParticipante));

        if (inscripcionRepo.findByParticipanteIdAndEventoId(participante.getId(), eventoId).isPresent()) {
            throw new IllegalArgumentException("Ya te encuentras inscrito en este evento");
        }

        long activosActuales = inscripcionRepo.countByEventoIdAndEstado(eventoId, Inscripcion.Estado.ACTIVA);
        boolean hayCupo = activosActuales < evento.getAforo();

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setParticipante(participante);
        inscripcion.setEvento(evento);
        inscripcion.setMetodoInscripcion(Inscripcion.MetodoInscripcion.FORMULARIO);
        inscripcion.setEstado(hayCupo ? Inscripcion.Estado.ACTIVA : Inscripcion.Estado.EN_ESPERA);

        Inscripcion guardada = inscripcionRepo.save(inscripcion);

        if (hayCupo) {
            String[] qr = qrService.generarQr(guardada.getId());
            guardada.setCodigoQr(qr[0]);
            guardada.setContenidoQr(qr[1]);
            guardada = inscripcionRepo.save(guardada);
        }

        auditoriaService.registrar(null, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.INSCRIPCION,
                guardada.getId(), "Inscripción de " + participante.getDocumento() + " al evento " + evento.getNombre()
                        + " (" + guardada.getEstado() + ")", null);

        return guardada;
    }

    public long posicionEnCola(Long inscripcionId, Long eventoId) {
        // Se usa en la respuesta al Invitado si quedó EN_ESPERA
        var enEspera = inscripcionRepo.findByEventoIdAndEstado(eventoId, Inscripcion.Estado.EN_ESPERA);
        for (int i = 0; i < enEspera.size(); i++) {
            if (enEspera.get(i).getId().equals(inscripcionId)) return i + 1;
        }
        return -1;
    }

    public List<InscripcionEsperaDTO> consultarCola(Long eventoId) {
        List<Inscripcion> cola = inscripcionRepo
                .findByEventoIdAndEstadoOrderByFechaInscripcionAsc(eventoId, Inscripcion.Estado.EN_ESPERA);

        List<InscripcionEsperaDTO> resultado = new java.util.ArrayList<>();
        int posicion = 1;
        for (Inscripcion i : cola) {
            InscripcionEsperaDTO dto = new InscripcionEsperaDTO();
            dto.setInscripcionId(i.getId());
            dto.setParticipanteNombre(i.getParticipante().getNombre() + " " + i.getParticipante().getApellido());
            dto.setParticipanteDocumento(i.getParticipante().getDocumento());
            dto.setFechaInscripcion(i.getFechaInscripcion());
            dto.setPosicion(posicion++);
            resultado.add(dto);
        }
        return resultado;
    }

    @org.springframework.transaction.annotation.Transactional
    public Inscripcion promover(Long inscripcionId, com.example.Eminent.usuarios.entity.Usuario ejecutor) throws Exception {
        Inscripcion inscripcion = inscripcionRepo.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        if (inscripcion.getEstado() == Inscripcion.Estado.ACTIVA) {
            throw new IllegalArgumentException("Esta inscripción ya se encuentra activa");
        }

        Evento evento = inscripcion.getEvento();
        long activosActuales = inscripcionRepo.countByEventoIdAndEstado(evento.getId(), Inscripcion.Estado.ACTIVA);
        if (activosActuales >= evento.getAforo()) {
            throw new IllegalArgumentException("No hay cupo disponible en este momento");
        }

        inscripcion.setEstado(Inscripcion.Estado.ACTIVA);
        String[] qr = qrService.generarQr(inscripcion.getId());
        inscripcion.setCodigoQr(qr[0]);
        inscripcion.setContenidoQr(qr[1]);
        Inscripcion actualizada = inscripcionRepo.save(inscripcion);

        auditoriaService.registrar(ejecutor, Auditoria.Accion.EDITAR, Auditoria.TipoAfectado.INSCRIPCION,
                actualizada.getId(), "Promoción de inscripción de " + inscripcion.getParticipante().getDocumento()
                        + " al evento " + evento.getNombre(), null);

        return actualizada;
    }

}
