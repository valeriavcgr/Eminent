package com.example.Eminent.certificacion.service;

import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.certificacion.entity.Certificado;
import com.example.Eminent.certificacion.repository.CertificadoRepository;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.EventoDia;
import com.example.Eminent.eventos.repository.EventoDiaRepository;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.entity.Inscripcion;
import com.example.Eminent.participacion.entity.Participante;
import com.example.Eminent.participacion.repository.InscripcionRepository;
import com.example.Eminent.participacion.repository.ParticipanteRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.Eminent.eventos.dto.EventoDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CertificacionService {

    @Autowired private AsistenciaRepository asistenciaRepo;
    @Autowired private CertificadoRepository certificadoRepo;
    @Autowired private EventoRepository eventoRepo;
    @Autowired private EventoDiaRepository eventoDiaRepo;
    @Autowired private InscripcionRepository inscripcionRepo;
    @Autowired private ParticipanteRepository participanteRepo;
    @Autowired private PdfService pdfService;
    @Autowired private AuditoriaService auditoriaService;
    private static final String CARPETA_QR_CERT = "qr-certificados";


    public static String formatearFechasEvento(LocalDateTime inicio, LocalDateTime fin) {
        Locale es = new Locale("es", "ES");
        DateTimeFormatter fmtCompleto = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", es);
        DateTimeFormatter fmtSoloDia = DateTimeFormatter.ofPattern("d", es);

        if (inicio.toLocalDate().equals(fin.toLocalDate())) {
            return inicio.format(fmtCompleto);
        } else if (inicio.getMonth() == fin.getMonth() && inicio.getYear() == fin.getYear()) {
            return "del " + inicio.format(fmtSoloDia) + " al " + fin.format(fmtCompleto);
        } else {
            return "del " + inicio.format(fmtCompleto) + " al " + fin.format(fmtCompleto);
        }
    }

    public void generarCertificadosDeEvento(Evento evento) throws Exception {
        List<EventoDia> jornadas = eventoDiaRepo.findByEvento_IdOrderByNumeroDiaAsc(evento.getId());
        int totalJornadas = jornadas.size();
        double horasTotales = jornadas.stream()
                .mapToDouble(j -> Duration.between(
                        LocalDateTime.of(j.getFecha(), j.getHoraInicio()),
                        LocalDateTime.of(j.getFecha(), j.getHoraFin())).toMinutes() / 60.0)
                .sum();
        BigDecimal duracionHoras = BigDecimal.valueOf(horasTotales).setScale(1, RoundingMode.HALF_UP);
        String duracionTexto = duracionHoras.stripTrailingZeros().toPlainString() + " horas";
        String fechasEventoTexto = formatearFechasEvento(evento.getFechaInicio(), evento.getFechaFin());

        List<Inscripcion> activas = inscripcionRepo
                .findByEventoIdAndEstado(evento.getId(), Inscripcion.Estado.ACTIVA, Pageable.unpaged())
                .getContent();

        int generados = 0;
        for (Inscripcion inscripcion : activas) {
            if (certificadoRepo.existsByInscripcion_Id(inscripcion.getId())) continue;

            long diasAsistidos = asistenciaRepo.countByInscripcion_Id(inscripcion.getId());
            if (totalJornadas == 0 || diasAsistidos < totalJornadas) continue;

            String codigoUnico = "CERT-" + evento.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String rutaQr = generarQrVerificacion(codigoUnico);

            Participante participante = inscripcion.getParticipante();
            String nombreCompleto = participante.getNombre() + " " + participante.getApellido();
            String fechaEmisionTexto = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            String rutaPdf = pdfService.generarPdf(nombreCompleto, evento.getNombre(),
                    fechasEventoTexto, duracionTexto, fechaEmisionTexto, codigoUnico, rutaQr);

            Certificado certificado = new Certificado();
            certificado.setInscripcion(inscripcion);
            certificado.setCodigoUnico(codigoUnico);
            certificado.setDuracionHoras(duracionHoras);
            certificado.setRutaPdf(rutaPdf);
            certificado.setCodigoQrVerificacion(rutaQr);
            certificadoRepo.save(certificado);

            generados++;
        }

        auditoriaService.registrar(null, Auditoria.Accion.CREAR, Auditoria.TipoAfectado.CERTIFICADO,
                evento.getId(), "Generación de certificados para evento " + evento.getNombre()
                        + ": " + generados + " certificados emitidos", null);
    }

    private String generarQrVerificacion(String codigoUnico) throws Exception {
        String contenido = "http://localhost:5173/certificados/verificar?codigo=" + codigoUnico;
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 300, 300);

        Path carpeta = Path.of(CARPETA_QR_CERT);
        if (!Files.exists(carpeta)) Files.createDirectories(carpeta);

        Path archivo = carpeta.resolve(codigoUnico + ".png");
        MatrixToImageWriter.writeToPath(matrix, "PNG", archivo);
        return archivo.toString();
    }

    public Certificado buscarPorDocumentoYEvento(String documento, Long eventoId) {
        Participante participante = participanteRepo.findByDocumento(documento)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún participante con ese documento"));

        Inscripcion inscripcion = inscripcionRepo.findByParticipanteIdAndEventoId(participante.getId(), eventoId)
                .orElseThrow(() -> new IllegalArgumentException("No hay certificado disponible para este evento"));

        return certificadoRepo.findByInscripcion_Id(inscripcion.getId())
                .orElseThrow(() -> new IllegalArgumentException("No hay certificado disponible para este evento"));
    }

    public Certificado verificarPorCodigo(String codigo) {
        return certificadoRepo.findByCodigoUnico(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Certificado no encontrado o inválido"));
    }

    public List<EventoDTO> eventosFinalizados() {
        return eventoRepo.findByEstado(Evento.Estado.FINALIZADO, Pageable.unpaged()).getContent().stream()
                .map(this::toEventoDTO)
                .toList();
    }

    private EventoDTO toEventoDTO(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setId(evento.getId());
        dto.setNombre(evento.getNombre());
        dto.setTipo(evento.getTipo().name());
        dto.setModalidad(evento.getModalidad().name());
        dto.setDescripcion(evento.getDescripcion());
        dto.setFechaInicio(evento.getFechaInicio());
        dto.setFechaFin(evento.getFechaFin());
        dto.setAforo(evento.getAforo());
        dto.setEstado(evento.getEstado().name());
        dto.setFechaCreacion(evento.getFechaCreacion());
        return dto;
    }
}