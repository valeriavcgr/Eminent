package com.example.Eminent.config;

import com.example.Eminent.asistencia.entity.Asistencia;
import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.certificacion.service.CertificacionService;
import com.example.Eminent.encuesta.entity.Encuesta;
import com.example.Eminent.encuesta.repository.EncuestaRepository;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.EventoDia;
import com.example.Eminent.eventos.entity.EventoMonitor;
import com.example.Eminent.eventos.repository.EventoDiaRepository;
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
    private final EventoDiaRepository eventoDiaRepository;
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

    /** Nombre fijo del evento demo de asistencia multi-día, usado para ubicarlo y regenerarlo en cada arranque. */
    private static final String NOMBRE_EVENTO_DEMO = "Evento Demo: Asistencia Multi-Día en Curso";
    /** Rango de documentos reservado para los participantes del evento demo (no colisiona con la semilla masiva). */
    private static final long DOCUMENTO_DEMO_INICIAL = 800000001L;
    private static final int PARTICIPANTES_DEMO = 10;

    public DataInitializer(UsuarioRepository usuarioRepository, RolRepository rolRepository, JdbcTemplate jdbcTemplate,
                           PasswordEncoder passwordEncoder,
                           EventoRepository eventoRepository, EventoDiaRepository eventoDiaRepository,
                           EventoMonitorRepository eventoMonitorRepository,
                           ParticipanteRepository participanteRepository, InscripcionRepository inscripcionRepository,
                           AsistenciaRepository asistenciaRepository, QrService qrService,
                           CertificacionService certificacionService, EncuestaRepository encuestaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.eventoRepository = eventoRepository;
        this.eventoDiaRepository = eventoDiaRepository;
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
        sembrarEventoDemoEnCurso();
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

        // --- 6 eventos FINALIZADOS de un día (horas distintas) ---
        for (int j = 0; j < 6; j++) {
            LocalDate diaEvento = LocalDate.now().minusDays(20 - j * 2);
            LocalTime horaInicio = LocalTime.of(8 + (j % 4), 0); // Ej: 08:00, 09:00, 10:00, 11:00
            LocalTime horaFin = horaInicio.plusHours(3);          // Duración fija de 3 horas

            Evento evento = crearEvento(
                    nombresFinalizados[j], tipos6[j], modalidades6[j],
                    List.of(new JornadaSpec(diaEvento, horaInicio, horaFin)),
                    Evento.Estado.FINALIZADO, creadores.get(j % creadores.size()));

            Usuario monitorAsignado = monitores.get(j % monitores.size());
            asignarMonitor(evento, monitorAsignado);

            List<EventoDia> jornadas = eventoDiaRepository.findByEvento_IdOrderByNumeroDiaAsc(evento.getId());
            // 5 de 10 participantes asisten (a su única jornada) y quedan certificados; de esos, 4 dejan encuesta.
            contadorDocumento = sembrarParticipantesDelEvento(evento, jornadas, contadorDocumento, 5, 5, monitorAsignado);

            certificacionService.generarCertificadosDeEvento(evento);
        }

        // --- 6 eventos PROGRAMADOS de un día (horas distintas, sin asistencia todavía) ---
        for (int j = 0; j < 6; j++) {
            LocalDate diaEvento = LocalDate.now().plusDays(10 + j * 2);
            LocalTime horaInicio = LocalTime.of(9 + (j % 3), 0); // Ej: 09:00, 10:00, 11:00
            LocalTime horaFin = horaInicio.plusHours(4);          // Duración fija de 4 horas

            Evento evento = crearEvento(
                    nombresProgramados[j], tipos6[j], modalidades6[j],
                    List.of(new JornadaSpec(diaEvento, horaInicio, horaFin)),
                    Evento.Estado.PROGRAMADO, creadores.get((j + 2) % creadores.size()));

            Usuario monitorAsignado = monitores.get((j + 1) % monitores.size());
            asignarMonitor(evento, monitorAsignado);

            List<EventoDia> jornadas = eventoDiaRepository.findByEvento_IdOrderByNumeroDiaAsc(evento.getId());
            contadorDocumento = sembrarParticipantesDelEvento(evento, jornadas, contadorDocumento, 0, 0, monitorAsignado);
        }

        // --- 2 eventos FINALIZADOS de 3 días, para probar asistencia completa vs parcial ---
        String[] nombresMultiDia = { "Capacitación Integral en Gestión de Proyectos", "Torneo Interno de Programación" };
        for (int j = 0; j < 2; j++) {
            LocalDate diaBase = LocalDate.now().minusDays(30 - j * 5);
            List<JornadaSpec> jornadaSpecs = List.of(
                    new JornadaSpec(diaBase, LocalTime.of(9, 0), LocalTime.of(17, 0)),
                    new JornadaSpec(diaBase.plusDays(1), LocalTime.of(9, 0), LocalTime.of(17, 0)),
                    new JornadaSpec(diaBase.plusDays(2), LocalTime.of(8, 0), LocalTime.of(13, 0))
            );

            Evento evento = crearEvento(
                    nombresMultiDia[j], tipos6[j % tipos6.length], modalidades6[j % modalidades6.length],
                    jornadaSpecs, Evento.Estado.FINALIZADO, creadores.get(j % creadores.size()));

            Usuario monitorAsignado = monitores.get(j % monitores.size());
            asignarMonitor(evento, monitorAsignado);

            List<EventoDia> jornadas = eventoDiaRepository.findByEvento_IdOrderByNumeroDiaAsc(evento.getId());
            // 4 de 10 asisten a las 3 jornadas (se certifican); 3 más asisten solo al primer día (asistencia parcial, sin certificado).
            contadorDocumento = sembrarParticipantesDelEvento(evento, jornadas, contadorDocumento, 4, 7, monitorAsignado);

            certificacionService.generarCertificadosDeEvento(evento);
        }

        // --- 3 eventos PROGRAMADOS de varios días (2 a 4 días, sin asistencia todavía) ---
        String[] nombresMultiDiaProgramados = {
                "Bootcamp Intensivo de Desarrollo Web",
                "Congreso de Innovación y Tecnología",
                "Torneo Nacional de Ajedrez por Equipos"
        };
        int[] duracionesDias = { 3, 4, 2 };
        for (int j = 0; j < 3; j++) {
            LocalDate diaBase = LocalDate.now().plusDays(25 + j * 8);
            List<JornadaSpec> jornadaSpecs = new ArrayList<>();
            for (int d = 0; d < duracionesDias[j]; d++) {
                jornadaSpecs.add(new JornadaSpec(diaBase.plusDays(d), LocalTime.of(8 + (d % 2), 0), LocalTime.of(16 + (d % 2), 0)));
            }

            Evento evento = crearEvento(
                    nombresMultiDiaProgramados[j], tipos6[j % tipos6.length], modalidades6[(j + 1) % modalidades6.length],
                    jornadaSpecs, Evento.Estado.PROGRAMADO, creadores.get((j + 1) % creadores.size()));

            Usuario monitorAsignado = monitores.get(j % monitores.size());
            asignarMonitor(evento, monitorAsignado);

            List<EventoDia> jornadas = eventoDiaRepository.findByEvento_IdOrderByNumeroDiaAsc(evento.getId());
            contadorDocumento = sembrarParticipantesDelEvento(evento, jornadas, contadorDocumento, 0, 0, monitorAsignado);
        }

        System.out.println("Semilla masiva completada: 4 usuarios, 17 eventos (12 de un día + 5 de varios días), 170 participantes.");
    }

    private record JornadaSpec(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
    }

    /**
     * Siembra un evento de ejemplo fijo, de varios días y EN_CURSO, con participantes ya inscritos
     * y asistencia parcial ya registrada. Se ejecuta en cada arranque del servidor (a diferencia de
     * {@link #sembrarEventosYParticipantes()}, que solo corre una vez): elimina la versión anterior
     * de este evento demo si existe y la recrea con fechas relativas a "ahora", para que siempre esté
     * en curso sin importar cuándo se levante el servidor.
     */
    private void sembrarEventoDemoEnCurso() throws Exception {
        limpiarEventoDemoAnterior();

        Usuario creador = usuarioRepository.findByCorreo("admin@eminent.com").orElseThrow();
        Usuario monitorAsignado = usuarioRepository.findByCorreo("monitor@eminent.com").orElseThrow();

        LocalDate hoy = LocalDate.now();
        List<JornadaSpec> jornadaSpecs = List.of(
                new JornadaSpec(hoy.minusDays(1), LocalTime.of(8, 0), LocalTime.of(17, 0)),
                new JornadaSpec(hoy, LocalTime.of(8, 0), LocalTime.of(17, 0)),
                new JornadaSpec(hoy.plusDays(1), LocalTime.of(8, 0), LocalTime.of(17, 0))
        );

        Evento evento = crearEvento(NOMBRE_EVENTO_DEMO, Evento.Tipo.CAPACITACION, Evento.Modalidad.PRESENCIAL,
                jornadaSpecs, Evento.Estado.EN_CURSO, creador);
        asignarMonitor(evento, monitorAsignado);

        List<EventoDia> jornadas = eventoDiaRepository.findByEvento_IdOrderByNumeroDiaAsc(evento.getId());

        // 10 participantes inscritos. Día 1 (ayer) ya cerró: 7 de 10 asistieron.
        // Día 2 (hoy) está en curso: de esos 7, solo 4 ya registraron asistencia hoy (asistencia parcial en vivo).
        // Día 3 (mañana) aún no ha empezado, por lo que nadie tiene asistencia todavía.
        for (int k = 0; k < PARTICIPANTES_DEMO; k++) {
            String documento = String.valueOf(DOCUMENTO_DEMO_INICIAL + k);
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

            if (k < 7) {
                registrarAsistenciaDemo(inscripcion, jornadas.get(0), monitorAsignado, k);
            }
            if (k < 4) {
                registrarAsistenciaDemo(inscripcion, jornadas.get(1), monitorAsignado, k);
            }
        }

        System.out.println("[DataInitializer] Evento demo multi-día en curso listo: " + NOMBRE_EVENTO_DEMO
                + " (jornadas " + jornadas.get(0).getFecha() + " a " + jornadas.get(jornadas.size() - 1).getFecha() + ")");
    }

    private void registrarAsistenciaDemo(Inscripcion inscripcion, EventoDia dia, Usuario monitor, int k) {
        Asistencia asistencia = new Asistencia();
        asistencia.setInscripcion(inscripcion);
        asistencia.setEventoDia(dia);
        asistencia.setFechaHora(LocalDateTime.of(dia.getFecha(), dia.getHoraInicio()).plusMinutes(20 + k * 10L));
        asistencia.setMetodo(k % 2 == 0 ? Asistencia.Metodo.MANUAL : Asistencia.Metodo.QR);
        asistencia.setRegistradoPor(monitor);
        asistenciaRepository.save(asistencia);
    }

    /** Borra el evento demo (y todo lo que depende de él) sembrado en un arranque anterior, si existe. */
    private void limpiarEventoDemoAnterior() {
        eventoRepository.findByNombreAndTipo(NOMBRE_EVENTO_DEMO, Evento.Tipo.CAPACITACION).ifPresent(evento -> {
            Long eventoId = evento.getId();
            jdbcTemplate.update(
                    "DELETE FROM certificado WHERE inscripcion_id IN (SELECT id FROM inscripcion WHERE evento_id = ?)",
                    eventoId);
            jdbcTemplate.update(
                    "DELETE FROM encuesta WHERE inscripcion_id IN (SELECT id FROM inscripcion WHERE evento_id = ?)",
                    eventoId);
            jdbcTemplate.update(
                    "DELETE FROM asistencia WHERE inscripcion_id IN (SELECT id FROM inscripcion WHERE evento_id = ?)",
                    eventoId);
            jdbcTemplate.update("DELETE FROM inscripcion WHERE evento_id = ?", eventoId);
            jdbcTemplate.update("DELETE FROM evento_monitor WHERE evento_id = ?", eventoId);
            jdbcTemplate.update("DELETE FROM evento_dia WHERE evento_id = ?", eventoId);
            jdbcTemplate.update("DELETE FROM evento WHERE id = ?", eventoId);
        });
        jdbcTemplate.update("DELETE FROM participante WHERE documento::bigint BETWEEN ? AND ?",
                DOCUMENTO_DEMO_INICIAL, DOCUMENTO_DEMO_INICIAL + PARTICIPANTES_DEMO - 1);
    }

    private Evento crearEvento(String nombre, Evento.Tipo tipo, Evento.Modalidad modalidad,
                               List<JornadaSpec> jornadaSpecs, Evento.Estado estado, Usuario creadoPor) {
        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setTipo(tipo);
        evento.setModalidad(modalidad);
        evento.setDescripcion("Evento de prueba generado por la semilla de datos.");
        evento.setAforo(15);
        evento.setEstado(estado);
        evento.setCreadoPor(creadoPor);

        LocalDateTime inicioProvisional = LocalDateTime.of(jornadaSpecs.get(0).fecha(), jornadaSpecs.get(0).horaInicio());
        LocalDateTime finProvisional = jornadaSpecs.stream()
                .map(j -> LocalDateTime.of(j.fecha(), j.horaFin()))
                .max(LocalDateTime::compareTo).orElseThrow();
        evento.setFechaInicio(inicioProvisional);
        evento.setFechaFin(finProvisional);
        evento = eventoRepository.save(evento);

        int numero = 1;
        List<EventoDia> jornadas = new ArrayList<>();
        for (JornadaSpec spec : jornadaSpecs) {
            EventoDia jornada = new EventoDia();
            jornada.setEvento(evento);
            jornada.setNumeroDia(numero++);
            jornada.setFecha(spec.fecha());
            jornada.setHoraInicio(spec.horaInicio());
            jornada.setHoraFin(spec.horaFin());
            jornadas.add(eventoDiaRepository.save(jornada));
        }

        LocalDateTime inicio = jornadas.stream()
                .map(j -> LocalDateTime.of(j.getFecha(), j.getHoraInicio())).min(LocalDateTime::compareTo).orElseThrow();
        LocalDateTime fin = jornadas.stream()
                .map(j -> LocalDateTime.of(j.getFecha(), j.getHoraFin())).max(LocalDateTime::compareTo).orElseThrow();
        evento.setFechaInicio(inicio);
        evento.setFechaFin(fin);
        return eventoRepository.save(evento);
    }

    private void asignarMonitor(Evento evento, Usuario monitor) {
        EventoMonitor asignacion = new EventoMonitor();
        asignacion.setEvento(evento);
        asignacion.setMonitor(monitor);
        eventoMonitorRepository.save(asignacion);
    }

    /**
     * Siembra 10 participantes inscritos en el evento. De ellos, los primeros
     * {@code completoHasta} asisten a TODAS las jornadas (quedan elegibles para certificado);
     * los siguientes hasta {@code parcialHasta} asisten solo a la primera jornada (asistencia
     * parcial, sin certificado); el resto no registra asistencia.
     */
    private int sembrarParticipantesDelEvento(Evento evento, List<EventoDia> jornadas, int contadorDocumentoInicial,
                                              int completoHasta, int parcialHasta, Usuario monitorParaAsistencia) throws Exception {
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

            if (k < parcialHasta) {
                List<EventoDia> diasAAsistir = (k < completoHasta) ? jornadas : jornadas.subList(0, 1);
                for (EventoDia dia : diasAAsistir) {
                    Asistencia asistencia = new Asistencia();
                    asistencia.setInscripcion(inscripcion);
                    asistencia.setEventoDia(dia);
                    asistencia.setFechaHora(LocalDateTime.of(dia.getFecha(), dia.getHoraInicio()).plusMinutes(20 + k * 10L));
                    asistencia.setMetodo(k % 2 == 0 ? Asistencia.Metodo.MANUAL : Asistencia.Metodo.QR);
                    asistencia.setRegistradoPor(monitorParaAsistencia);
                    asistenciaRepository.save(asistencia);
                }

                if (k < completoHasta && k < 4) {
                    int idx = (int) ((evento.getId() + k) % COMENTARIOS_ENCUESTA.length);
                    Encuesta encuesta = new Encuesta();
                    encuesta.setInscripcion(inscripcion);
                    encuesta.setCalificacion(CALIFICACIONES_ENCUESTA[idx]);
                    encuesta.setComentario(COMENTARIOS_ENCUESTA[idx]);
                    encuestaRepository.save(encuesta);
                }
            }
        }

        return contadorDocumento;
    }
}