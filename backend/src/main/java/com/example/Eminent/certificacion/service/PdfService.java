package com.example.Eminent.certificacion.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfService {

    private static final String CARPETA_PDF = "certificados-pdf";

    public String generarPdf(String nombreParticipante, String nombreEvento,
                             String duracion, String fechaEmision, String codigoUnico,
                             String rutaQr) throws IOException {
        Path carpeta = Path.of(CARPETA_PDF);
        if (!Files.exists(carpeta)) Files.createDirectories(carpeta);

        Path archivo = carpeta.resolve("certificado-" + codigoUnico + ".pdf");

        try (PdfWriter writer = new PdfWriter(archivo.toString());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4.rotate())) {

            document.setMargins(40, 50, 40, 50);

            PdfFont fuenteTitulo = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont fuenteNormal = PdfFontFactory.createFont("Helvetica");

            // 1. Dibujar fondo y marcos elegantes en la página
            PdfCanvas canvas = new PdfCanvas(pdf.getPage(1));
            Rectangle pageSize = pdf.getPage(1).getPageSize();
            float w = pageSize.getWidth();
            float h = pageSize.getHeight();

            // Rellenar fondo con color suave acorde a la landing (#F8F9FF)
            canvas.setFillColor(new DeviceRgb(248, 249, 255))
                    .rectangle(0, 0, w, h)
                    .fill();

            // Borde exterior Azul (#2563EB)
            canvas.setStrokeColor(new DeviceRgb(37, 99, 235))
                    .setLineWidth(3)
                    .rectangle(18, 18, w - 36, h - 36)
                    .stroke();

            // Borde interior Dorado (#D97706)
            canvas.setStrokeColor(new DeviceRgb(217, 119, 6))
                    .setLineWidth(1)
                    .rectangle(24, 24, w - 48, h - 48)
                    .stroke();

            // 2. Agregar contenido textual estructurado
            document.add(new Paragraph("EMINENT")
                    .setFont(fuenteTitulo)
                    .setFontColor(new DeviceRgb(37, 99, 235))
                    .setFontSize(32)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30));

            document.add(new Paragraph("CERTIFICADO DE PARTICIPACIÓN")
                    .setFont(fuenteTitulo)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2));

            document.add(new Paragraph("Otorgado con orgullo a:")
                    .setFont(fuenteNormal)
                    .setFontColor(ColorConstants.GRAY)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));

            document.add(new Paragraph(nombreParticipante)
                    .setFont(fuenteTitulo)
                    .setFontColor(ColorConstants.BLACK)
                    .setFontSize(26)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5));

            document.add(new Paragraph("Por haber completado exitosamente el curso/taller:")
                    .setFont(fuenteNormal)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(15));

            document.add(new Paragraph(nombreEvento)
                    .setFont(fuenteTitulo)
                    .setFontColor(new DeviceRgb(37, 99, 235))
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5));

            document.add(new Paragraph("Fecha del evento: " + duracion + " | Fecha de emisión: " + fechaEmision + " | Código de verificación: " + codigoUnico)
                    .setFont(fuenteNormal)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(15));

            // 3. Código QR en el centro
            Image qrImagen = new Image(ImageDataFactory.create(rutaQr));
            qrImagen.setWidth(85);
            qrImagen.setHeight(85);
            qrImagen.setHorizontalAlignment(HorizontalAlignment.CENTER);
            qrImagen.setMarginTop(30);
            document.add(qrImagen);

            document.add(new Paragraph("Escanea para verificar la autenticidad")
                    .setFont(fuenteNormal)
                    .setFontColor(ColorConstants.GRAY)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5));
        }

        return archivo.toString();
    }
}