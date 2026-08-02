package com.example.Eminent.participacion.service;

import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.dto.FilaCsvDTO;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.entity.Participante;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.participacion.repository.ParticipanteRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class CsvImportService {

    @Autowired private EventoRepository eventoRepo;
    @Autowired private ParticipanteRepository participanteRepo;
    @Autowired private InscripcionRepository inscripcionRepo;
    @Autowired private QrService qrService;
    @Autowired private AuditoriaService auditoriaService;

    private static final Pattern PATRON_DOCUMENTO = Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    public List<FilaCsvDTO> previsualizar(MultipartFile archivo, Long eventoId) throws Exception {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        validarEventoImportable(evento);

        List<FilaCsvDTO> filas = leerArchivo(archivo);
        long cupoRestante = evento.getAforo() - inscripcionRepo.countByEventoIdAndEstado(eventoId, Inscripcion.Estado.ACTIVA);

        for (FilaCsvDTO fila : filas) {
            validarFila(fila);

            if (fila.isValida() && yaEstaInscrito(fila.getDocumento(), eventoId)) {
                fila.setValida(false);
                fila.setMotivoError("Ya existe una inscripción para este documento en este evento");
            }

            if (fila.isValida()) {
                if (cupoRestante <= 0) {
                    fila.setIraListaEspera(true);
                } else {
                    cupoRestante--;
                }
            }
        }
        return filas;
    }

    public Map<String, Object> confirmar(MultipartFile archivo, Long eventoId, Usuario ejecutor) throws Exception {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        validarEventoImportable(evento);

        List<FilaCsvDTO> filas = leerArchivo(archivo);
        int exitosas = 0;
        int enEspera = 0;
        int fallidas = 0;

        for (FilaCsvDTO fila : filas) {
            validarFila(fila);
            if (!fila.isValida()) {
                fallidas++;
                continue;
            }

            if (yaEstaInscrito(fila.getDocumento(), eventoId)) {
                fallidas++;
                continue;
            }

            Participante participante = participanteRepo.findByDocumento(fila.getDocumento())
                    .orElseGet(() -> {
                        Participante p = new Participante();
                        p.setNombre(fila.getNombre());
                        p.setApellido(fila.getApellido());
                        p.setDocumento(fila.getDocumento());
                        p.setCorreo(fila.getCorreo());
                        p.setTelefono(fila.getTelefono());
                        return participanteRepo.save(p);
                    });

            long activos = inscripcionRepo.countByEventoIdAndEstado(eventoId, Inscripcion.Estado.ACTIVA);
            boolean hayCupo = activos < evento.getAforo();

            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setParticipante(participante);
            inscripcion.setEvento(evento);
            inscripcion.setMetodoInscripcion(Inscripcion.MetodoInscripcion.CSV);
            inscripcion.setEstado(hayCupo ? Inscripcion.Estado.ACTIVA : Inscripcion.Estado.EN_ESPERA);
            Inscripcion guardada = inscripcionRepo.save(inscripcion);

            if (hayCupo) {
                String[] qr = qrService.generarQr(guardada.getId());
                guardada.setCodigoQr(qr[0]);
                guardada.setContenidoQr(qr[1]);
                inscripcionRepo.save(guardada);
                exitosas++;
            } else {
                enEspera++;
            }
        }

        auditoriaService.registrar(ejecutor, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.INSCRIPCION,
                eventoId, "Importación masiva CSV al evento " + evento.getNombre()
                        + ": " + exitosas + " activas, " + enEspera + " en espera, " + fallidas + " fallidas", null);

        return Map.of("exitosas", exitosas, "enEspera", enEspera, "fallidas", fallidas);
    }

    private boolean yaEstaInscrito(String documento, Long eventoId) {
        return participanteRepo.findByDocumento(documento)
                .map(p -> inscripcionRepo.findByParticipanteIdAndEventoId(p.getId(), eventoId).isPresent())
                .orElse(false);
    }

    private void validarEventoImportable(Evento evento) {
        if (evento.getEstado() == Evento.Estado.FINALIZADO) {
            throw new IllegalArgumentException("No se puede importar participantes a un evento finalizado");
        }
    }

    private List<FilaCsvDTO> leerArchivo(MultipartFile archivo) throws Exception {
        List<FilaCsvDTO> filas = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(archivo.getInputStream()))) {
            String[] linea;
            int numeroFila = 0;
            reader.readNext();
            while ((linea = reader.readNext()) != null) {
                numeroFila++;
                FilaCsvDTO fila = new FilaCsvDTO();
                fila.setNumeroFila(numeroFila);
                fila.setNombre(linea.length > 0 ? linea[0].trim() : "");
                fila.setApellido(linea.length > 1 ? linea[1].trim() : "");
                fila.setDocumento(linea.length > 2 ? linea[2].trim() : "");
                fila.setCorreo(linea.length > 3 ? linea[3].trim() : "");
                fila.setTelefono(linea.length > 4 ? linea[4].trim() : "");
                filas.add(fila);
            }
        }
        return filas;
    }

    private void validarFila(FilaCsvDTO fila) {
        if (fila.getNombre().isBlank() || fila.getApellido().isBlank()) {
            fila.setValida(false);
            fila.setMotivoError("Nombre y apellido son obligatorios");
            return;
        }

        if (fila.getDocumento().isBlank()) {
            fila.setValida(false);
            fila.setMotivoError("El documento es obligatorio");
            return;
        }

        if (!PATRON_DOCUMENTO.matcher(fila.getDocumento()).matches()) {
            fila.setValida(false);
            fila.setMotivoError("El documento debe ser numérico, mínimo 8 dígitos");
            return;
        }

        if (!fila.getCorreo().isBlank() && !PATRON_CORREO.matcher(fila.getCorreo()).matches()) {
            fila.setValida(false);
            fila.setMotivoError("El formato del correo electrónico no es válido");
            return;
        }

        fila.setValida(true);
        fila.setMotivoError(null);
    }
}