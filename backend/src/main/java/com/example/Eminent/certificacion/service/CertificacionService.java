package com.example.Eminent.certificacion.service;

import com.example.Eminent.asistencia.entity.Asistencia;
import com.example.Eminent.asistencia.repository.AsistenciaRepository;
import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.service.AuditoriaService;
import com.example.Eminent.certificacion.entity.Certificado;
import com.example.Eminent.certificacion.repository.CertificadoRepository;
import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.repository.EventoRepository;
import com.example.Eminent.participacion.entity.Participante;
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
    @Autowired private ParticipanteRepository participanteRepo;
    @Autowired private PdfService pdfService;
    @Autowired private AuditoriaService auditoriaService;
    private static final String CARPETA_QR_CERT = "qr-certificados";

    /**
     * Da formato a las fechas de un evento: "2 de marzo de 2026" si es un solo día,
     * o "del 2 al 5 de marzo de 2026" si dura varios días.
     */
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
        List<Asistencia> asistencias = asistenciaRepo.findByInscripcion_Evento_Id(evento.getId());

        int generados = 0;
        for (Asistencia asistencia : asistencias) {
            if (certificadoRepo.findByAsistenciaId(asistencia.getId()).isPresent()) continue;

            String codigoUnico = "CERT-" + evento.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            long horasTotales = Duration.between(evento.getFechaInicio(), evento.getFechaFin()).toHours();
            long dias = Math.max(1, (long) Math.ceil(horasTotales / 24.0));
            BigDecimal duracionDias = BigDecimal.valueOf(dias);

            String fechasEventoTexto = formatearFechasEvento(evento.getFechaInicio(), evento.getFechaFin());

            String rutaQr = generarQrVerificacion(codigoUnico);

            Participante participante = asistencia.getInscripcion().getParticipante();
            String nombreCompleto = participante.getNombre() + " " + participante.getApellido();
            String fechaEmisionTexto = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            String rutaPdf = pdfService.generarPdf(nombreCompleto, evento.getNombre(),
                    fechasEventoTexto, fechaEmisionTexto, codigoUnico, rutaQr);

            Certificado certificado = new Certificado();
            certificado.setAsistencia(asistencia);
            certificado.setCodigoUnico(codigoUnico);
            certificado.setDuracionHoras(duracionDias);
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

        Asistencia asistencia = asistenciaRepo
                .findByInscripcion_Participante_IdAndInscripcion_Evento_Id(participante.getId(), eventoId)
                .orElseThrow(() -> new IllegalArgumentException("No hay certificado disponible para este evento"));

        return certificadoRepo.findByAsistenciaId(asistencia.getId())
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