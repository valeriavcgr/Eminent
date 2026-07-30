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

    private static final Pattern PATRON_DOCUMENTO = Pattern.compile("^[0-9]{4,15}$");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    public List<FilaCsvDTO> previsualizar(MultipartFile archivo, Long eventoId) throws Exception {
        eventoRepo.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        List<FilaCsvDTO> filas = leerArchivo(archivo);
        for (FilaCsvDTO fila : filas) {
            validarFila(fila);
        }
        return filas;
    }

    public Map<String, Object> confirmar(MultipartFile archivo, Long eventoId, Usuario ejecutor) throws Exception {
        Evento evento = eventoRepo.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        List<FilaCsvDTO> filas = leerArchivo(archivo);
        int exitosas = 0;
        int fallidas = 0;

        for (FilaCsvDTO fila : filas) {
            validarFila(fila);
            if (!fila.isValida()) {
                fallidas++;
                continue;
            }

            long activos = inscripcionRepo.countByEventoIdAndEstado(eventoId, Inscripcion.Estado.ACTIVA);
            if (activos >= evento.getAforo()) {
                fila.setValida(false);
                fila.setMotivoError("No importada por cupo lleno");
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

            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setParticipante(participante);
            inscripcion.setEvento(evento);
            inscripcion.setMetodoInscripcion(Inscripcion.MetodoInscripcion.CSV);
            inscripcion.setEstado(Inscripcion.Estado.ACTIVA);
            Inscripcion guardada = inscripcionRepo.save(inscripcion);

            String[] qr = qrService.generarQr(guardada.getId());
            guardada.setCodigoQr(qr[0]);
            guardada.setContenidoQr(qr[1]);
            inscripcionRepo.save(guardada);

            exitosas++;
        }

        auditoriaService.registrar(ejecutor, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.INSCRIPCION,
                eventoId, "Importación masiva CSV al evento " + evento.getNombre()
                        + ": " + exitosas + " exitosas, " + fallidas + " fallidas", null);

        return Map.of("exitosas", exitosas, "fallidas", fallidas);
    }

    private List<FilaCsvDTO> leerArchivo(MultipartFile archivo) throws Exception {
        List<FilaCsvDTO> filas = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(archivo.getInputStream()))) {
            String[] linea;
            int numeroFila = 0;
            reader.readNext(); // saltar encabezado
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
        if (fila.getNombre().isBlank() || fila.getApellido().isBlank() || fila.getDocumento().isBlank()) {
            fila.setValida(false);
            fila.setMotivoError("Nombre, apellido y documento son obligatorios");
            return;
        }

        if (!PATRON_DOCUMENTO.matcher(fila.getDocumento()).matches()) {
            fila.setValida(false);
            fila.setMotivoError("El documento debe ser numérico (4 a 15 dígitos)");
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