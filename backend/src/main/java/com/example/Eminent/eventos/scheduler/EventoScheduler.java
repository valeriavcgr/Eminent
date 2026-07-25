package com.example.Eminent.eventos.scheduler;

import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.repository.EventoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventoScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventoScheduler.class);

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cambiarEstadosEventos() {
        LocalDateTime ahora = LocalDateTime.now();

        List<Evento> programados = eventoRepository.findByEstadoAndFechaInicioLessThanEqual(
                Evento.Estado.PROGRAMADO, ahora);

        for (Evento evento : programados) {
            evento.setEstado(Evento.Estado.EN_CURSO);
            eventoRepository.save(evento);
            auditoriaService.registrar(null, Auditoria.Accion.EDITAR, Auditoria.TipoAfectado.EVENTO,
                    evento.getId(), "Cambio de estado automático: PROGRAMADO → EN_CURSO", null);
            log.info("Evento [{}] cambiado de PROGRAMADO a EN_CURSO", evento.getNombre());
        }

        List<Evento> enCurso = eventoRepository.findByEstadoAndFechaFinLessThanEqual(
                Evento.Estado.EN_CURSO, ahora);

        for (Evento evento : enCurso) {
            evento.setEstado(Evento.Estado.FINALIZADO);
            eventoRepository.save(evento);
            auditoriaService.registrar(null, Auditoria.Accion.EDITAR, Auditoria.TipoAfectado.EVENTO,
                    evento.getId(), "Cambio de estado automático: EN_CURSO → FINALIZADO", null);
            log.info("Evento [{}] cambiado de EN_CURSO a FINALIZADO", evento.getNombre());
            // TODO (RF-22): Disparar generación automática de certificados aquí
            // cuando el módulo Certificación esté implementado.
            // auditoriaService.registrar(null, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.CERTIFICADO,
            //     evento.getId(), "Generación de certificados para evento " + evento.getNombre(), null);
        }

        if (programados.isEmpty() && enCurso.isEmpty()) {
            log.debug("EventoScheduler ejecutado sin cambios de estado");
        }
    }
}