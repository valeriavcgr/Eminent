package com.example.Eminent.certificacion.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Servicio para la generación de certificados en formato PDF.
 * Crea un documento PDF con el nombre del participante, evento,
 * duración, fecha de emisión, código de verificación y código QR
 * de autenticidad. Los archivos se almacenan en la carpeta certificados-pdf.
 */
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
             Document document = new Document(pdf)) {

            PdfFont fuenteTitulo = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont fuenteNormal = PdfFontFactory.createFont("Helvetica");

            // 1. Primero se agrega TODO el contenido (esto es lo que crea la página 1)
            document.add(new Paragraph("CERTIFICADO DE PARTICIPACIÓN")
                    .setFont(fuenteTitulo)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            document.add(new Paragraph(nombreParticipante)
                    .setFont(fuenteTitulo)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("Curso: " + nombreEvento)
                    .setFont(fuenteNormal).setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Duración: " + duracion + " horas")
                    .setFont(fuenteNormal).setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Fecha de emisión: " + fechaEmision)
                    .setFont(fuenteNormal).setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Código de verificación: " + codigoUnico)
                    .setFont(fuenteNormal).setFontSize(12).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            Image qrImagen = new Image(ImageDataFactory.create(rutaQr));
            qrImagen.setWidth(100);
            document.add(qrImagen);


            PdfCanvas canvas = new PdfCanvas(pdf.getPage(1));
            Rectangle pageSize = pdf.getPage(1).getPageSize();
            canvas.setStrokeColor(ColorConstants.DARK_GRAY)
                    .setLineWidth(3)
                    .rectangle(20, 20, pageSize.getWidth() - 40, pageSize.getHeight() - 40)
                    .stroke();
        }

        return archivo.toString();
    }
}