package com.example.Eminent.certificacion.service;

import com.itextpdf.kernel.colors.ColorConstants;
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
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfService {

    private static final String CARPETA_PDF = "certificados-pdf";

    // Paleta oscura, inspirada en el mismo tono de la landing
    private static final DeviceRgb FONDO_OSCURO = new DeviceRgb(15, 23, 42);   // slate-900
    private static final DeviceRgb ACENTO_CLARO = new DeviceRgb(96, 165, 250); // blue-400
    private static final DeviceRgb TEXTO_CLARO = new DeviceRgb(241, 245, 249); // slate-100
    private static final DeviceRgb TEXTO_GRIS = new DeviceRgb(148, 163, 184);  // slate-400
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

            document.setMargins(35, 60, 35, 60);

            PdfFont fuenteTitulo = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont fuenteNormal = PdfFontFactory.createFont("Helvetica");
            PdfFont fuenteCursiva = PdfFontFactory.createFont("Helvetica-Oblique");
            PdfFont fuenteTituloCursiva = PdfFontFactory.createFont("Helvetica-BoldOblique");

            // 1. Crear la página explícitamente, para poder dibujar el fondo antes que nada
            PdfPage pagina = pdf.addNewPage(PageSize.A4.rotate());
            Rectangle pageSize = pagina.getPageSize();
            float w = pageSize.getWidth();
            float h = pageSize.getHeight();

            // 2. Fondo azul marino oscuro — primero, para que quede debajo de todo el texto
            PdfCanvas fondo = new PdfCanvas(pagina);
            fondo.setFillColor(FONDO_OSCURO)
                    .rectangle(0, 0, w, h)
                    .fill();

            // 3. Texto e imagen — colores claros para que resalten sobre el fondo oscuro
            document.add(new Paragraph("EMINENT")
                    .setFont(fuenteTitulo)
                    .setFontColor(ACENTO_CLARO)
                    .setFontSize(36)
                    .setCharacterSpacing(2)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(40)
                    .setMarginBottom(0));

            document.add(new Paragraph("CERTIFICADO DE PARTICIPACIÓN")
                    .setFont(fuenteTituloCursiva)
                    .setFontColor(TEXTO_GRIS)
                    .setFontSize(15)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(3)
                    .setMarginBottom(0));

            document.add(new Paragraph(" ")
                    .setBorderBottom(new SolidBorder(DORADO_ACENTO, 1.5f))
                    .setWidth(220)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setMarginTop(12)
                    .setMarginBottom(0)
                    .setFontSize(2));

            document.add(new Paragraph("Otorgado con orgullo a")
                    .setFont(fuenteCursiva)
                    .setFontColor(TEXTO_GRIS)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(16)
                    .setMarginBottom(0));

            document.add(new Paragraph(nombreParticipante)
                    .setFont(fuenteTitulo)
                    .setFontColor(TEXTO_CLARO)
                    .setFontSize(27)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5)
                    .setMarginBottom(0));

            document.add(new Paragraph("por su participación en")
                    .setFont(fuenteCursiva)
                    .setFontColor(TEXTO_GRIS)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(14)
                    .setMarginBottom(0));

            document.add(new Paragraph(nombreEvento)
                    .setFont(fuenteTitulo)
                    .setFontColor(ACENTO_CLARO)
                    .setFontSize(19)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5)
                    .setMarginBottom(0));

            document.add(new Paragraph(fechasEvento)
                    .setFont(fuenteNormal)
                    .setFontColor(TEXTO_GRIS)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(8)
                    .setMarginBottom(0));

            document.add(new Paragraph("Duración: " + duracion)
                    .setFont(fuenteNormal)
                    .setFontColor(TEXTO_GRIS)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2)
                    .setMarginBottom(0));

            // El QR necesita un fondo blanco propio para seguir siendo legible
            // sobre el fondo oscuro — se pinta automáticamente con el flujo del
            // documento, sin coordenadas manuales (evita riesgos de posicionamiento).
            Image qrImagen = new Image(ImageDataFactory.create(rutaQr));
            qrImagen.setWidth(75);
            qrImagen.setHeight(75);
            qrImagen.setBackgroundColor(ColorConstants.WHITE);
            qrImagen.setPadding(8);
            qrImagen.setHorizontalAlignment(HorizontalAlignment.CENTER);
            qrImagen.setMarginTop(18);
            qrImagen.setMarginBottom(0);
            document.add(qrImagen);

            document.add(new Paragraph("Fecha de emisión: " + fechaEmision)
                    .setFont(fuenteNormal)
                    .setFontColor(TEXTO_GRIS)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(6)
                    .setMarginBottom(0));

            document.add(new Paragraph("Código de verificación: " + codigoUnico)
                    .setFont(fuenteCursiva)
                    .setFontColor(TEXTO_GRIS)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2)
                    .setMarginBottom(0));

            // 4. Bordes — al final, con el documento todavía abierto (página ya existe con certeza)
            PdfCanvas bordes = new PdfCanvas(pdf.getPage(1));
            bordes.setStrokeColor(ACENTO_CLARO)
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