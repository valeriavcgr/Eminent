package com.example.Eminent.eventos.service;
import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.eventos.dto.EventoDTO;
import com.example.Eminent.eventos.dto.EventoDiaDTO;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.EventoDia;
import com.example.Eminent.eventos.entity.EventoMonitor;
import com.example.Eminent.eventos.repository.EventoDiaRepository;
import com.example.Eminent.eventos.repository.EventoMonitorRepository;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class EventosService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoMonitorRepository eventoMonitorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private EventoDiaRepository eventoDiaRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    private void validarJornadas(List<EventoDiaDTO> jornadas) {
        if (jornadas == null || jornadas.isEmpty()) {
            throw new IllegalArgumentException("El evento debe tener al menos una jornada");
        }
        Set<java.time.LocalDate> fechasVistas = new HashSet<>();
        for (EventoDiaDTO j : jornadas) {
            if (j.getFecha() == null || j.getHoraInicio() == null || j.getHoraFin() == null) {
                throw new IllegalArgumentException("Cada jornada requiere fecha, hora de inicio y hora de fin");
            }
            if (!j.getHoraFin().isAfter(j.getHoraInicio())) {
                throw new IllegalArgumentException("La hora de fin de cada jornada debe ser posterior a la hora de inicio");
            }
            if (!fechasVistas.add(j.getFecha())) {
                throw new IllegalArgumentException("No puede haber dos jornadas con la misma fecha (" + j.getFecha() + ")");
            }
        }
    }

    private void recalcularRangoEvento(Evento evento, List<EventoDia> jornadas) {
        LocalDateTime inicio = jornadas.stream()
                .map(j -> LocalDateTime.of(j.getFecha(), j.getHoraInicio()))
                .min(LocalDateTime::compareTo)
                .orElseThrow();
        LocalDateTime fin = jornadas.stream()
                .map(j -> LocalDateTime.of(j.getFecha(), j.getHoraFin()))
                .max(LocalDateTime::compareTo)
                .orElseThrow();
        evento.setFechaInicio(inicio);
        evento.setFechaFin(fin);
    }

    @Transactional
    public Evento crear(EventoDTO dto, Usuario usuario) {
        if (dto.getNombre() == null || dto.getNombre().isBlank() ||
                dto.getTipo() == null || dto.getModalidad() == null ||
                dto.getAforo() == null) {
            throw new IllegalArgumentException("Todos los campos son obligatorios");
        }

        if (eventoRepository.existsByNombreAndTipo(dto.getNombre(), Evento.Tipo.valueOf(dto.getTipo()))) {
            throw new IllegalArgumentException("Ya existe un evento con este nombre para el tipo seleccionado");
        }

        validarJornadas(dto.getJornadas());

        Evento evento = new Evento();
        evento.setNombre(dto.getNombre());
        evento.setTipo(Evento.Tipo.valueOf(dto.getTipo()));
        evento.setModalidad(Evento.Modalidad.valueOf(dto.getModalidad()));
        evento.setDescripcion(dto.getDescripcion());
        evento.setAforo(dto.getAforo());
        evento.setEstado(Evento.Estado.PROGRAMADO);
        evento.setCreadoPor(usuario);
        evento.setFechaCreacion(LocalDateTime.now());

        List<EventoDiaDTO> jornadasOrdenadas = dto.getJornadas().stream()
                .sorted((a, b) -> a.getFecha().compareTo(b.getFecha()))
                .toList();
        LocalDateTime inicioProvisional = LocalDateTime.of(jornadasOrdenadas.get(0).getFecha(), jornadasOrdenadas.get(0).getHoraInicio());
        LocalDateTime finProvisional = jornadasOrdenadas.stream()
                .map(j -> LocalDateTime.of(j.getFecha(), j.getHoraFin()))
                .max(LocalDateTime::compareTo).orElseThrow();
        evento.setFechaInicio(inicioProvisional);
        evento.setFechaFin(finProvisional);

        Evento guardado = eventoRepository.save(evento);

        List<EventoDia> jornadasGuardadas = new java.util.ArrayList<>();
        int numero = 1;
        for (EventoDiaDTO j : jornadasOrdenadas) {
            EventoDia jornada = new EventoDia();
            jornada.setEvento(guardado);
            jornada.setNumeroDia(numero++);
            jornada.setFecha(j.getFecha());
            jornada.setHoraInicio(j.getHoraInicio());
            jornada.setHoraFin(j.getHoraFin());
            jornadasGuardadas.add(eventoDiaRepository.save(jornada));
        }

        recalcularRangoEvento(guardado, jornadasGuardadas);
        guardado = eventoRepository.save(guardado);

        auditoriaService.registrar(usuario, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.EVENTO,
                guardado.getId(), "Creación de evento " + guardado.getNombre() + " tipo " + guardado.getTipo(), null);

        return guardado;
    }

    public List<EventoDia> listarJornadas(Long eventoId) {
        return eventoDiaRepository.findByEvento_IdOrderByNumeroDiaAsc(eventoId);
    }

    private void sincronizarJornadas(Evento evento, List<EventoDiaDTO> dtoJornadas) {
        validarJornadas(dtoJornadas);

        List<EventoDia> existentes = eventoDiaRepository.findByEvento_IdOrderByNumeroDiaAsc(evento.getId());
        Map<Long, EventoDia> porId = new HashMap<>();
        for (EventoDia e : existentes) porId.put(e.getId(), e);
        Set<Long> idsEnPayload = new HashSet<>();

        for (EventoDiaDTO d : dtoJornadas) {
            if (d.getId() != null && porId.containsKey(d.getId())) {
                EventoDia existente = porId.get(d.getId());
                boolean tieneAsistencias = asistenciaRepository.existsByEventoDia_Id(existente.getId());
                if (tieneAsistencias && (!existente.getFecha().equals(d.getFecha())
                        || !existente.getHoraInicio().equals(d.getHoraInicio())
                        || !existente.getHoraFin().equals(d.getHoraFin()))) {
                    throw new IllegalArgumentException("No se puede modificar una jornada que ya registró asistencias");
                }
                existente.setFecha(d.getFecha());
                existente.setHoraInicio(d.getHoraInicio());
                existente.setHoraFin(d.getHoraFin());
                eventoDiaRepository.save(existente);
                idsEnPayload.add(existente.getId());
            } else {
                EventoDia nueva = new EventoDia();
                nueva.setEvento(evento);
                nueva.setFecha(d.getFecha());
                nueva.setHoraInicio(d.getHoraInicio());
                nueva.setHoraFin(d.getHoraFin());
                eventoDiaRepository.save(nueva);
            }
        }

        for (EventoDia existente : existentes) {
            if (!idsEnPayload.contains(existente.getId())) {
                if (asistenciaRepository.existsByEventoDia_Id(existente.getId())) {
                    throw new IllegalArgumentException("No se puede eliminar una jornada que ya registró asistencias");
                }
                eventoDiaRepository.delete(existente);
            }
        }

        List<EventoDia> actualizadas = eventoDiaRepository.findByEvento_IdOrderByFechaAsc(evento.getId());
        for (int i = 0; i < actualizadas.size(); i++) {
            actualizadas.get(i).setNumeroDia(i + 1);
        }
        eventoDiaRepository.saveAll(actualizadas);

        recalcularRangoEvento(evento, actualizadas);
    }

    public Evento obtenerPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con id " + id));
    }

    public Page<Evento> listarParaAdminOperador(Pageable pageable) {
        return eventoRepository.findAll(pageable);
    }

    public Page<Evento> listarParaMonitor(Long monitorId, Pageable pageable) {
        List<EventoMonitor> asignaciones = eventoMonitorRepository.findByMonitor_Id(monitorId);
        List<Evento> eventos = asignaciones.stream().map(EventoMonitor::getEvento).toList();
        return new org.springframework.data.domain.PageImpl<>(eventos, pageable, eventos.size());
    }

    public Page<Evento> listarPublicos(Pageable pageable) {
        List<Evento> eventos = eventoRepository.findByEstadoIn(
                List.of(Evento.Estado.PROGRAMADO, Evento.Estado.EN_CURSO), Pageable.unpaged()).getContent();
        return new org.springframework.data.domain.PageImpl<>(eventos, pageable, eventos.size());
    }

    public Page<Evento> listarConFiltros(Evento.Tipo tipo, Evento.Modalidad modalidad, Evento.Estado estado,
                                                  LocalDateTime fechaDesde, LocalDateTime fechaHasta,
                                                  Usuario usuario, Usuario.Rol rolActivo,
                                                  Pageable pageable) {
        LocalDateTime desde = (fechaDesde != null) ? fechaDesde : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime hasta = (fechaHasta != null) ? fechaHasta : LocalDateTime.of(2100, 1, 1, 0, 0);
        Long creadoPorId = (rolActivo == Usuario.Rol.OPERADOR) ? usuario.getId() : null;
        return eventoRepository.buscarConFiltros(tipo, modalidad, estado, desde, hasta, creadoPorId, pageable);
    }

    @Transactional
    public Evento editar(Long id, EventoDTO dto, Usuario usuario) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con id " + id));

        if (evento.getEstado() == Evento.Estado.FINALIZADO) {
            throw new IllegalArgumentException("No se puede modificar un evento que ya terminó");
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank() && dto.getTipo() != null) {
            Evento.Tipo tipo = Evento.Tipo.valueOf(dto.getTipo());
            boolean nombreCambiado = !evento.getNombre().equals(dto.getNombre());
            boolean tipoCambiado = !evento.getTipo().equals(tipo);
            if (nombreCambiado || tipoCambiado) {
                if (eventoRepository.existsByNombreAndTipo(
                        nombreCambiado ? dto.getNombre() : evento.getNombre(),
                        tipoCambiado ? tipo : evento.getTipo())) {
                    throw new IllegalArgumentException("Ya existe un evento con este nombre para el tipo seleccionado");
                }
            }
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            evento.setNombre(dto.getNombre());
        }
        if (dto.getTipo() != null) {
            evento.setTipo(Evento.Tipo.valueOf(dto.getTipo()));
        }
        if (dto.getModalidad() != null) {
            evento.setModalidad(Evento.Modalidad.valueOf(dto.getModalidad()));
        }
        if (dto.getDescripcion() != null) {
            evento.setDescripcion(dto.getDescripcion());
        }
        if (dto.getAforo() != null) {
            long inscritosActivos = inscripcionRepository.countByEventoIdAndEstado(id, Inscripcion.Estado.ACTIVA);
            if (dto.getAforo() < inscritosActivos) {
                throw new IllegalArgumentException("El aforo no puede ser menor a la cantidad de inscritos actuales");
            }

            evento.setAforo(dto.getAforo());
        }
        if (dto.getJornadas() != null) {
            sincronizarJornadas(evento, dto.getJornadas());
        }

        Evento actualizado = eventoRepository.save(evento);

        auditoriaService.registrar(usuario, Auditoria.Accion.EDITAR, Auditoria.TipoAfectado.EVENTO,
                actualizado.getId(), "Edición de evento " + actualizado.getNombre(), null);

        return actualizado;
    }

    @Transactional
    public EventoMonitor asignarMonitor(Long eventoId, Long monitorId, Usuario usuario) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con id " + eventoId));

        if (evento.getEstado() == Evento.Estado.FINALIZADO || evento.getEstado() == Evento.Estado.CANCELADO) {
            throw new IllegalArgumentException("No se puede asignar personal a un evento finalizado o cancelado");
        }

        Usuario monitor = usuarioRepository.findById(monitorId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id " + monitorId));

        if (!monitor.tieneRol(Usuario.Rol.MONITOR)) {
            throw new IllegalArgumentException("Solo usuarios con rol MONITOR pueden ser asignados como monitor");
        }

        if (monitor.getEstado() != Usuario.Estado.ACTIVO) {
            throw new IllegalArgumentException("El monitor debe estar en estado ACTIVO");
        }

        if (eventoMonitorRepository.existsByEvento_IdAndMonitor_Id(eventoId, monitorId)) {
            throw new IllegalArgumentException("Este monitor ya está asignado a este evento");
        }

        EventoMonitor asignacion = new EventoMonitor();
        asignacion.setEvento(evento);
        asignacion.setMonitor(monitor);
        asignacion.setFechaAsignacion(LocalDateTime.now());

        EventoMonitor guardado = eventoMonitorRepository.save(asignacion);

        auditoriaService.registrar(usuario, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.EVENTO,
                eventoId, "Asignación de monitor " + monitor.getNombre() + " al evento " + evento.getNombre(), null);

        return guardado;
    }

    @Transactional
    public Evento cancelar(Long id, Usuario usuario) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con id " + id));

        if (evento.getEstado() != Evento.Estado.PROGRAMADO) {
            throw new IllegalArgumentException("No es posible cancelar un evento que ya ha iniciado o finalizado");
        }

        long inscritosActivos = inscripcionRepository.countByEventoIdAndEstado(id, Inscripcion.Estado.ACTIVA);
        if (inscritosActivos > 0) {
            throw new IllegalArgumentException("No se puede cancelar un evento que ya cuenta con inscritos");
        }

        evento.setEstado(Evento.Estado.CANCELADO);
        Evento actualizado = eventoRepository.save(evento);

        auditoriaService.registrar(usuario, Auditoria.Accion.CANCELAR, Auditoria.TipoAfectado.EVENTO,
                actualizado.getId(), "Cancelación del evento " + actualizado.getNombre(), null);

        return actualizado;
    }
}