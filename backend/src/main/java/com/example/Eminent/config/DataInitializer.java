package com.example.Eminent.config;

import com.example.Eminent.asistencia.entity.Asistencia;
import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.certificacion.service.CertificacionService;
import com.example.Eminent.encuesta.entity.Encuesta;
import com.example.Eminent.encuesta.repository.EncuestaRepository;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.EventoMonitor;
import com.example.Eminent.eventos.repository.EventoMonitorRepository;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.entity.Participante;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.participacion.repository.ParticipanteRepository;
import com.example.Eminent.participacion.service.QrService;
import com.example.Eminent.usuarios.entity.Rol;
import com.example.Eminent.usuarios.entity.Usuario;
import com.example.Eminent.usuarios.repository.RolRepository;
import com.example.Eminent.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EventoRepository eventoRepository;
    private final EventoMonitorRepository eventoMonitorRepository;
    private final ParticipanteRepository participanteRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final QrService qrService;
    private final CertificacionService certificacionService;
    private final EncuestaRepository encuestaRepository;

    private static final String[] NOMBRES = {
            "Carlos", "María", "Luis", "Ana", "Javier", "Sofia", "Diego", "Valentina", "Andrés", "Laura"
    };
    private static final String[] APELLIDOS = {
            "Gómez", "Rodríguez", "Pérez", "Martínez", "López", "Torres", "García", "Ramírez", "Vargas", "Hernández"
    };

    private static final String[] COMENTARIOS_ENCUESTA = {
            "Excelente organización, aprendí mucho.",
            "Muy buen contenido, aunque el tiempo se sintió un poco corto.",
            "El facilitador explicó todo muy claro, lo recomiendo.",
            "Buena experiencia en general, me gustaría más ejercicios prácticos.",
            "Todo estuvo muy bien organizado, ¡gracias!",
            "Superó mis expectativas, muy útil para el día a día.",
            "Estuvo bien, aunque la conexión virtual tuvo algunos problemas.",
            "Aprendí bastante, me gustaría que hubiera una segunda parte.",
            "Buena dinámica y buen manejo del tiempo.",
            "Cumplió lo prometido, volvería a participar."
    };
    private static final int[] CALIFICACIONES_ENCUESTA = { 5, 4, 5, 4, 5, 5, 3, 4, 5, 4 };

    public DataInitializer(UsuarioRepository usuarioRepository, RolRepository rolRepository, JdbcTemplate jdbcTemplate,
                           PasswordEncoder passwordEncoder,
                           EventoRepository eventoRepository, EventoMonitorRepository eventoMonitorRepository,
                           ParticipanteRepository participanteRepository, InscripcionRepository inscripcionRepository,
                           AsistenciaRepository asistenciaRepository, QrService qrService,
                           CertificacionService certificacionService, EncuestaRepository encuestaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.eventoRepository = eventoRepository;
        this.eventoMonitorRepository = eventoMonitorRepository;
        this.participanteRepository = participanteRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.qrService = qrService;
        this.certificacionService = certificacionService;
        this.encuestaRepository = encuestaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        relajarColumnaRolLegada();
        repararForeignKeyAuditoriaUsuario();
        repararCheckTipoAfectadoAuditoria();
        repararCheckAforoEvento();
        repararCheckCalificacionEncuesta();
        repararUniqueInscripcionParticipanteEvento();
        sembrarCatalogoRoles();
        migrarRolUnicoAUsuarioRoles();
        sembrarUsuarios();
        sembrarEventosYParticipantes();
    }

    private void relajarColumnaRolLegada() {
        jdbcTemplate.execute("ALTER TABLE usuario ALTER COLUMN rol DROP NOT NULL");
    }

    private void repararForeignKeyAuditoriaUsuario() {
        jdbcTemplate.execute("""
                DO $$
                DECLARE
                    nombre_restriccion text;
                    regla_borrado text;
                BEGIN
                    SELECT rc.constraint_name, rc.delete_rule INTO nombre_restriccion, regla_borrado
                    FROM information_schema.referential_constraints rc
                    JOIN information_schema.key_column_usage kcu ON kcu.constraint_name = rc.constraint_name
                    WHERE rc.constraint_schema = 'public'
                      AND kcu.table_name = 'auditoria'
                      AND kcu.column_name = 'usuario_id'
                    LIMIT 1;

                    IF nombre_restriccion IS NOT NULL AND regla_borrado <> 'SET NULL' THEN
                        EXECUTE 'ALTER TABLE auditoria DROP CONSTRAINT ' || quote_ident(nombre_restriccion);
                        EXECUTE 'ALTER TABLE auditoria ADD CONSTRAINT fk_auditoria_usuario ' ||
                                'FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL';
                    END IF;
                END $$;
                """);
    }

    private void repararCheckTipoAfectadoAuditoria() {
        jdbcTemplate.execute("ALTER TABLE auditoria DROP CONSTRAINT IF EXISTS auditoria_tipo_afectado_check");
        jdbcTemplate.execute(
                """
                        ALTER TABLE auditoria ADD CONSTRAINT auditoria_tipo_afectado_check
                        CHECK (tipo_afectado IN ('USUARIO','EVENTO','PARTICIPANTE','INSCRIPCION','ASISTENCIA','CERTIFICADO','ENCUESTA'))
                        """);
    }

    private void repararCheckAforoEvento() {
        jdbcTemplate.execute("ALTER TABLE evento DROP CONSTRAINT IF EXISTS evento_aforo_check");
        jdbcTemplate.execute("ALTER TABLE evento ADD CONSTRAINT evento_aforo_check CHECK (aforo > 0)");
    }

    private void repararCheckCalificacionEncuesta() {
        jdbcTemplate.execute("ALTER TABLE encuesta DROP CONSTRAINT IF EXISTS encuesta_calificacion_check");
        jdbcTemplate.execute(
                "ALTER TABLE encuesta ADD CONSTRAINT encuesta_calificacion_check CHECK (calificacion BETWEEN 1 AND 5)");
    }

    private void repararUniqueInscripcionParticipanteEvento() {
        jdbcTemplate
                .execute("ALTER TABLE inscripcion DROP CONSTRAINT IF EXISTS inscripcion_participante_evento_unique");
        jdbcTemplate.execute(
                "ALTER TABLE inscripcion ADD CONSTRAINT inscripcion_participante_evento_unique UNIQUE (participante_id, evento_id)");
    }

    private void sembrarCatalogoRoles() {
        for (Usuario.Rol nombre : Usuario.Rol.values()) {
            rolRepository.findByNombre(nombre).orElseGet(() -> rolRepository.save(new Rol(nombre)));
        }
    }

    private void migrarRolUnicoAUsuarioRoles() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        int migrados = 0;
        for (Usuario usuario : usuarios) {
            if (!usuario.getRoles().isEmpty() || usuario.getRolLegado() == null)
                continue;
            Rol rolEntidad = rolRepository.findByNombre(usuario.getRolLegado())
                    .orElseThrow(() -> new IllegalStateException("Catálogo de roles incompleto"));
            usuario.getRoles().add(rolEntidad);
            usuarioRepository.save(usuario);
            migrados++;
        }
        if (migrados > 0) {
            System.out
                    .println("[DataInitializer] Migrados " + migrados + " usuario(s) de rol único a roles múltiples.");
        }
    }

    private void sembrarUsuarios() {
        crearOActualizarUsuarioSeed("admin@eminent.com", "Camila", "Restrepo", "Admin1234", "+573001234567",
                Usuario.Rol.ADMIN);
        crearOActualizarUsuarioSeed("operador@eminent.com", "Sebastián", "Duarte", "Operador1", "+573002345678",
                Usuario.Rol.OPERADOR);
        crearOActualizarUsuarioSeed("monitor@eminent.com", "Daniela", "Salazar", "Monitor12", "+573003456789",
                Usuario.Rol.MONITOR);
        crearOActualizarUsuarioSeed("supervisor@eminent.com", "Mateo", "Cárdenas", "Supervisor1", "+573004567890",
                Usuario.Rol.ADMIN, Usuario.Rol.OPERADOR, Usuario.Rol.MONITOR);
    }

    private void crearOActualizarUsuarioSeed(String correo, String nombre, String apellido, String contrasena,
                                             String telefono, Usuario.Rol... roles) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setCorreo(correo);
            for (Usuario.Rol rol : roles) {
                usuario.getRoles().add(rolRepository.findByNombre(rol).orElseThrow());
            }
            usuario.setTelefono(telefono);
            usuario.setEstado(Usuario.Estado.ACTIVO);
            usuario.setContrasena(passwordEncoder.encode(contrasena));
            usuarioRepository.save(usuario);
            System.out.println("[DataInitializer] Usuario creado: " + correo + " (" + List.of(roles) + ")");
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
        creadores.add(usuarioRepository.findByCorreo("operador@eminent.com").orElseThrow());
        creadores.add(usuarioRepository.findByCorreo("supervisor@eminent.com").orElseThrow());

        List<Usuario> monitores = new ArrayList<>();
        monitores.add(usuarioRepository.findByCorreo("monitor@eminent.com").orElseThrow());
        monitores.add(usuarioRepository.findByCorreo("supervisor@eminent.com").orElseThrow());

        Evento.Tipo[] tipos6 = {
                Evento.Tipo.TALLER, Evento.Tipo.CAPACITACION, Evento.Tipo.TORNEO,
                Evento.Tipo.TALLER, Evento.Tipo.CAPACITACION, Evento.Tipo.TORNEO
        };
        Evento.Modalidad[] modalidades6 = {
                Evento.Modalidad.PRESENCIAL, Evento.Modalidad.VIRTUAL, Evento.Modalidad.PRESENCIAL,
                Evento.Modalidad.VIRTUAL, Evento.Modalidad.PRESENCIAL, Evento.Modalidad.VIRTUAL
        };

        String[] nombresFinalizados = {
                "Taller de Liderazgo y Trabajo en Equipo",
                "Capacitación en Excel Avanzado",
                "Torneo de Ajedrez Corporativo",
                "Taller de Oratoria y Comunicación Efectiva",
                "Capacitación en Seguridad de la Información",
                "Torneo de Trivia Corporativa"
        };
        String[] nombresProgramados = {
                "Taller de Manejo del Estrés Laboral",
                "Capacitación en Atención al Cliente",
                "Torneo de Fútbol 5 Interáreas",
                "Taller de Innovación y Creatividad",
                "Capacitación en Gestión del Tiempo",
                "Torneo de Preguntados Virtual"
        };

        int contadorDocumento = 900000001;

        // --- 6 eventos FINALIZADOS (un mismo día, horas distintas) ---
        for (int j = 0; j < 6; j++) {
            LocalDate diaEvento = LocalDate.now().minusDays(20 - j * 2);
            LocalTime horaInicio = LocalTime.of(8 + (j % 4), 0); // Ej: 08:00, 09:00, 10:00, 11:00
            LocalTime horaFin = horaInicio.plusHours(3);          // Duración fija de 3 horas el mismo día

            LocalDateTime inicio = LocalDateTime.of(diaEvento, horaInicio);
            LocalDateTime fin = LocalDateTime.of(diaEvento, horaFin);

            Evento evento = crearEvento(
                    nombresFinalizados[j],
                    tipos6[j], modalidades6[j], inicio, fin, Evento.Estado.FINALIZADO,
                    creadores.get(j % creadores.size()));

            Usuario monitorAsignado = monitores.get(j % monitores.size());
            asignarMonitor(evento, monitorAsignado);

            contadorDocumento = sembrarParticipantesDelEvento(evento, contadorDocumento, true, monitorAsignado);

            certificacionService.generarCertificadosDeEvento(evento);
        }

        // --- 6 eventos PROGRAMADOS (un mismo día, horas distintas) ---
        for (int j = 0; j < 6; j++) {
            LocalDate diaEvento = LocalDate.now().plusDays(10 + j * 2);
            LocalTime horaInicio = LocalTime.of(9 + (j % 3), 0); // Ej: 09:00, 10:00, 11:00
            LocalTime horaFin = horaInicio.plusHours(4);          // Duración fija de 4 horas el mismo día

            LocalDateTime inicio = LocalDateTime.of(diaEvento, horaInicio);
            LocalDateTime fin = LocalDateTime.of(diaEvento, horaFin);

            Evento evento = crearEvento(
                    nombresProgramados[j],
                    tipos6[j], modalidades6[j], inicio, fin, Evento.Estado.PROGRAMADO,
                    creadores.get((j + 2) % creadores.size()));

            Usuario monitorAsignado = monitores.get((j + 1) % monitores.size());
            asignarMonitor(evento, monitorAsignado);

            contadorDocumento = sembrarParticipantesDelEvento(evento, contadorDocumento, false, monitorAsignado);
        }

        System.out.println("Semilla masiva completada: 4 usuarios, 12 eventos de un día, 120 participantes, 24 encuestas.");
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
        evento.setAforo(15);
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

            if (marcarAsistencia && k < 5) {
                Asistencia asistencia = new Asistencia();
                asistencia.setInscripcion(inscripcion);
                asistencia.setFechaHora(evento.getFechaInicio().plusMinutes(20 + k * 10L));
                asistencia.setMetodo(k % 2 == 0 ? Asistencia.Metodo.MANUAL : Asistencia.Metodo.QR);
                asistencia.setRegistradoPor(monitorParaAsistencia);
                asistencia = asistenciaRepository.save(asistencia);

                if (k < 4) {
                    int idx = (int) ((evento.getId() + k) % COMENTARIOS_ENCUESTA.length);
                    Encuesta encuesta = new Encuesta();
                    encuesta.setAsistencia(asistencia);
                    encuesta.setCalificacion(CALIFICACIONES_ENCUESTA[idx]);
                    encuesta.setComentario(COMENTARIOS_ENCUESTA[idx]);
                    encuestaRepository.save(encuesta);
                }
            }
        }

        return contadorDocumento;
    }
}