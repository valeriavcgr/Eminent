package com.example.Eminent.config;

import com.example.Eminent.asistencia.entity.Asistencia;
import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.certificacion.service.CertificacionService;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.EventoMonitor;
import com.example.Eminent.eventos.repository.EventoMonitorRepository;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.entity.Participante;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.participacion.repository.ParticipanteRepository;
import com.example.Eminent.participacion.service.QrService;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventoRepository eventoRepository;
    private final EventoMonitorRepository eventoMonitorRepository;
    private final ParticipanteRepository participanteRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final QrService qrService;
    private final CertificacionService certificacionService;

    private static final String[] NOMBRES = {
            "Carlos", "María", "Luis", "Ana", "Javier", "Sofia", "Diego", "Valentina", "Andrés", "Laura"
    };
    private static final String[] APELLIDOS = {
            "Gómez", "Rodríguez", "Pérez", "Martínez", "López", "Torres", "García", "Ramírez", "Vargas", "Hernández"
    };

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           EventoRepository eventoRepository, EventoMonitorRepository eventoMonitorRepository,
                           ParticipanteRepository participanteRepository, InscripcionRepository inscripcionRepository,
                           AsistenciaRepository asistenciaRepository, QrService qrService,
                           CertificacionService certificacionService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventoRepository = eventoRepository;
        this.eventoMonitorRepository = eventoMonitorRepository;
        this.participanteRepository = participanteRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.qrService = qrService;
        this.certificacionService = certificacionService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        sembrarUsuarios();
        sembrarEventosYParticipantes();
    }

    private void sembrarUsuarios() {
        crearOActualizarUsuarioSeed("admin@eminent.com", "Admin1234", Usuario.Rol.ADMIN, "+573001234567");
        crearOActualizarUsuarioSeed("admin2@eminent.com", "Admin1234", Usuario.Rol.ADMIN, "+573001234568");

        crearOActualizarUsuarioSeed("operador@eminent.com", "Operador1", Usuario.Rol.OPERADOR, "+573002345678");
        crearOActualizarUsuarioSeed("operador2@eminent.com", "Operador1", Usuario.Rol.OPERADOR, "+573002345679");
        crearOActualizarUsuarioSeed("operador3@eminent.com", "Operador1", Usuario.Rol.OPERADOR, "+573002345680");

        crearOActualizarUsuarioSeed("monitor@eminent.com", "Monitor12", Usuario.Rol.MONITOR, "+573003456789");
        crearOActualizarUsuarioSeed("monitor2@eminent.com", "Monitor12", Usuario.Rol.MONITOR, "+573003456790");
        crearOActualizarUsuarioSeed("monitor3@eminent.com", "Monitor12", Usuario.Rol.MONITOR, "+573003456791");
        crearOActualizarUsuarioSeed("monitor4@eminent.com", "Monitor12", Usuario.Rol.MONITOR, "+573003456792");
    }

    private void crearOActualizarUsuarioSeed(String correo, String contrasena, Usuario.Rol rol, String telefono) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setNombre(rol.name());
            usuario.setApellido("User");
            usuario.setCorreo(correo);
            usuario.setRol(rol);
            usuario.setTelefono(telefono);
            usuario.setEstado(Usuario.Estado.ACTIVO);
            usuario.setContrasena(passwordEncoder.encode(contrasena));
            usuarioRepository.save(usuario);
            System.out.println("[DataInitializer] Usuario creado: " + correo + " (" + rol + ")");
        } else if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            usuario.setContrasena(passwordEncoder.encode(contrasena));
            usuarioRepository.save(usuario);
            System.out.println("[DataInitializer] Contraseña resincronizada: " + correo);
        }
    }

    private void sembrarEventosYParticipantes() throws Exception {
        if (eventoRepository.count() > 0) {
            System.out.println("[DataInitializer] Ya existen eventos — se omite la semilla masiva.");
            return;
        }

        List<Usuario> creadores = new ArrayList<>();
        creadores.add(usuarioRepository.findByCorreo("admin@eminent.com").orElseThrow());
        creadores.add(usuarioRepository.findByCorreo("admin2@eminent.com").orElseThrow());
        creadores.add(usuarioRepository.findByCorreo("operador@eminent.com").orElseThrow());
        creadores.add(usuarioRepository.findByCorreo("operador2@eminent.com").orElseThrow());
        creadores.add(usuarioRepository.findByCorreo("operador3@eminent.com").orElseThrow());

        List<Usuario> monitores = new ArrayList<>();
        monitores.add(usuarioRepository.findByCorreo("monitor@eminent.com").orElseThrow());
        monitores.add(usuarioRepository.findByCorreo("monitor2@eminent.com").orElseThrow());
        monitores.add(usuarioRepository.findByCorreo("monitor3@eminent.com").orElseThrow());
        monitores.add(usuarioRepository.findByCorreo("monitor4@eminent.com").orElseThrow());

        Evento.Tipo[] tipos6 = {
                Evento.Tipo.TALLER, Evento.Tipo.CAPACITACION, Evento.Tipo.TORNEO,
                Evento.Tipo.TALLER, Evento.Tipo.CAPACITACION, Evento.Tipo.TORNEO
        };
        Evento.Modalidad[] modalidades6 = {
                Evento.Modalidad.PRESENCIAL, Evento.Modalidad.VIRTUAL, Evento.Modalidad.PRESENCIAL,
                Evento.Modalidad.VIRTUAL, Evento.Modalidad.PRESENCIAL, Evento.Modalidad.VIRTUAL
        };

        int contadorDocumento = 900000001; // arranca en un documento base, único y creciente

        // --- 6 eventos FINALIZADOS (fechas en el pasado) ---
        for (int j = 0; j < 6; j++) {
            LocalDateTime inicio = LocalDateTime.now().minusDays(20 - j * 2).withHour(9).withMinute(0);
            LocalDateTime fin = inicio.plusHours(4);

            Evento evento = crearEvento(
                    "Evento Finalizado " + (j + 1) + " - " + tipos6[j].name(),
                    tipos6[j], modalidades6[j], inicio, fin, Evento.Estado.FINALIZADO,
                    creadores.get(j % creadores.size())
            );

            Usuario monitorAsignado = monitores.get(j % monitores.size());
            asignarMonitor(evento, monitorAsignado);

            contadorDocumento = sembrarParticipantesDelEvento(evento, contadorDocumento, true, monitorAsignado);

            // Dispara la generación real de certificados para los que sí asistieron
            certificacionService.generarCertificadosDeEvento(evento);
        }

        //  eventos PROGRAMADOS (fechas en el futuro)
        for (int j = 0; j < 6; j++) {
            LocalDateTime inicio = LocalDateTime.now().plusDays(10 + j * 2).withHour(9).withMinute(0);
            LocalDateTime fin = inicio.plusHours(4);

            Evento evento = crearEvento(
                    "Evento Programado " + (j + 1) + " - " + tipos6[j].name(),
                    tipos6[j], modalidades6[j], inicio, fin, Evento.Estado.PROGRAMADO,
                    creadores.get((j + 2) % creadores.size())
            );

            Usuario monitorAsignado = monitores.get((j + 1) % monitores.size());
            asignarMonitor(evento, monitorAsignado);

            contadorDocumento = sembrarParticipantesDelEvento(evento, contadorDocumento, false, monitorAsignado);
        }

        System.out.println("Semilla masiva completada: 9 usuarios, 12 eventos, 120 participantes.");
    }

    private Evento crearEvento(String nombre, Evento.Tipo tipo, Evento.Modalidad modalidad,
                               LocalDateTime inicio, LocalDateTime fin, Evento.Estado estado, Usuario creadoPor) {
        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setTipo(tipo);
        evento.setModalidad(modalidad);
        evento.setDescripcion("Evento de prueba generado por la semilla de datos.");
        evento.setFechaInicio(inicio);
        evento.setFechaFin(fin);
        evento.setAforo(15); // 10 invitados usados, deja 5 cupos libres
        evento.setEstado(estado);
        evento.setCreadoPor(creadoPor);
        return eventoRepository.save(evento);
    }

    private void asignarMonitor(Evento evento, Usuario monitor) {
        EventoMonitor asignacion = new EventoMonitor();
        asignacion.setEvento(evento);
        asignacion.setMonitor(monitor);
        eventoMonitorRepository.save(asignacion);
    }


    private int sembrarParticipantesDelEvento(Evento evento, int contadorDocumentoInicial,
                                              boolean marcarAsistencia, Usuario monitorParaAsistencia) throws Exception {
        int contadorDocumento = contadorDocumentoInicial;

        for (int k = 0; k < 10; k++) {
            String documento = String.valueOf(contadorDocumento++);
            String nombre = NOMBRES[k % NOMBRES.length];
            String apellido = APELLIDOS[k % APELLIDOS.length];

            Participante participante = new Participante();
            participante.setNombre(nombre);
            participante.setApellido(apellido);
            participante.setDocumento(documento);
            participante.setCorreo(nombre.toLowerCase() + "." + apellido.toLowerCase() + documento + "@ejemplo.com");
            participante.setTelefono("+57" + documento);
            participante = participanteRepository.save(participante);

            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setParticipante(participante);
            inscripcion.setEvento(evento);
            inscripcion.setMetodoInscripcion(Inscripcion.MetodoInscripcion.FORMULARIO);
            inscripcion.setEstado(Inscripcion.Estado.ACTIVA);
            inscripcion.setFechaInscripcion(evento.getFechaInicio().minusDays(3));
            inscripcion = inscripcionRepository.save(inscripcion);

            String[] qr = qrService.generarQr(inscripcion.getId());
            inscripcion.setCodigoQr(qr[0]);
            inscripcion.setContenidoQr(qr[1]);
            inscripcionRepository.save(inscripcion);

            // Solo en eventos finalizados: la mitad (los primeros 5) sí asistió
            if (marcarAsistencia && k < 5) {
                Asistencia asistencia = new Asistencia();
                asistencia.setInscripcion(inscripcion);
                asistencia.setFechaHora(evento.getFechaInicio().plusMinutes(20 + k * 10L));
                asistencia.setMetodo(k % 2 == 0 ? Asistencia.Metodo.MANUAL : Asistencia.Metodo.QR);
                asistencia.setRegistradoPor(monitorParaAsistencia);
                asistenciaRepository.save(asistencia);
            }
        }

        return contadorDocumento;
    }
}