package com.example.Eminent.certificacion.service;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
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

    // Paleta de la marca EMINENT
    private static final DeviceRgb AZUL_MARCA = new DeviceRgb(37, 99, 235);
    private static final DeviceRgb AZUL_OSCURO_TEXTO = new DeviceRgb(15, 23, 42);
    private static final DeviceRgb AZUL_FONDO = new DeviceRgb(224, 242, 254);
    private static final DeviceRgb GRIS_TEXTO = new DeviceRgb(71, 85, 105);
    private static final DeviceRgb DORADO_ACENTO = new DeviceRgb(217, 119, 6);

    public String generarPdf(String nombreParticipante, String nombreEvento,
                             String fechasEvento, String duracion, String fechaEmision, String codigoUnico,
                             String rutaQr) throws IOException {
        Path carpeta = Path.of(CARPETA_PDF);
        if (!Files.exists(carpeta)) Files.createDirectories(carpeta);

        Path archivo = carpeta.resolve("certificado-" + codigoUnico + ".pdf");

        try (PdfWriter writer = new PdfWriter(archivo.toString());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4.rotate())) {

            document.setMargins(45, 60, 45, 60);

            PdfFont fuenteTitulo = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont fuenteNormal = PdfFontFactory.createFont("Helvetica");
            PdfFont fuenteCursiva = PdfFontFactory.createFont("Helvetica-Oblique");
            PdfFont fuenteTituloCursiva = PdfFontFactory.createFont("Helvetica-BoldOblique");

            // 1. Crear la página explícitamente, para poder dibujar el fondo antes que nada
            PdfPage pagina = pdf.addNewPage(PageSize.A4.rotate());
            Rectangle pageSize = pagina.getPageSize();
            float w = pageSize.getWidth();
            float h = pageSize.getHeight();

            // 2. Fondo azul clarito — primero, para que quede debajo de todo el texto
            PdfCanvas fondo = new PdfCanvas(pagina);
            fondo.setFillColor(AZUL_FONDO)
                    .rectangle(0, 0, w, h)
                    .fill();

            // 3. Texto e imagen — se dibujan encima del fondo, con jerarquía de tipografías
            document.add(new Paragraph("EMINENT")
                    .setFont(fuenteTitulo)
                    .setFontColor(AZUL_MARCA)
                    .setFontSize(40)
                    .setCharacterSpacing(2)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(35));

            document.add(new Paragraph("CERTIFICADO DE PARTICIPACIÓN")
                    .setFont(fuenteTituloCursiva)
                    .setFontColor(GRIS_TEXTO)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2));

            // Línea decorativa dorada, centrada, generada por el propio flujo del documento
            // (evita cualquier cálculo manual de posición, para no repetir bugs anteriores)
            document.add(new Paragraph(" ")
                    .setBorderBottom(new SolidBorder(DORADO_ACENTO, 1.5f))
                    .setWidth(220)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setMarginTop(14)
                    .setMarginBottom(0));

            document.add(new Paragraph("Otorgado con orgullo a")
                    .setFont(fuenteCursiva)
                    .setFontColor(GRIS_TEXTO)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));

            document.add(new Paragraph(nombreParticipante)
                    .setFont(fuenteTitulo)
                    .setFontColor(AZUL_OSCURO_TEXTO)
                    .setFontSize(30)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(6));

            document.add(new Paragraph("por su participación en")
                    .setFont(fuenteCursiva)
                    .setFontColor(GRIS_TEXTO)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(18));

            document.add(new Paragraph(nombreEvento)
                    .setFont(fuenteTitulo)
                    .setFontColor(AZUL_MARCA)
                    .setFontSize(21)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(6));

            document.add(new Paragraph(fechasEvento)
                    .setFont(fuenteNormal)
                    .setFontColor(GRIS_TEXTO)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10));

            document.add(new Paragraph("Duración: " + duracion)
                    .setFont(fuenteNormal)
                    .setFontColor(GRIS_TEXTO)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2));

            Image qrImagen = new Image(ImageDataFactory.create(rutaQr));
            qrImagen.setWidth(80);
            qrImagen.setHeight(80);
            qrImagen.setHorizontalAlignment(HorizontalAlignment.CENTER);
            qrImagen.setMarginTop(24);
            document.add(qrImagen);

            document.add(new Paragraph("Código de verificación: " + codigoUnico)
                    .setFont(fuenteCursiva)
                    .setFontColor(GRIS_TEXTO)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(6));

            // 4. Bordes — al final, con el documento todavía abierto (página ya existe con certeza)
            PdfCanvas bordes = new PdfCanvas(pdf.getPage(1));
            bordes.setStrokeColor(AZUL_MARCA)
                    .setLineWidth(3)
                    .rectangle(18, 18, w - 36, h - 36)
                    .stroke();
            bordes.setStrokeColor(DORADO_ACENTO)
                    .setLineWidth(1)
                    .rectangle(24, 24, w - 48, h - 48)
                    .stroke();
        }

        return archivo.toString();
    }
}