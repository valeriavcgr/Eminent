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
                             String fechasEvento, String duracion, String fechaEmision, String codigoUnico,
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

            // 1. Crear la página explícitamente, para poder dibujar el fondo antes que nada
            PdfPage pagina = pdf.addNewPage(PageSize.A4.rotate());
            Rectangle pageSize = pagina.getPageSize();
            float w = pageSize.getWidth();
            float h = pageSize.getHeight();

            // 2. Fondo de color — primero, para que quede debajo del texto
            PdfCanvas fondo = new PdfCanvas(pagina);
            fondo.setFillColor(new DeviceRgb(248, 249, 255))
                    .rectangle(0, 0, w, h)
                    .fill();

            // 3. Texto e imagen — se dibujan encima del fondo
            document.add(new Paragraph("EMINENT")
                    .setFont(fuenteTitulo)
                    .setFontColor(new DeviceRgb(37, 99, 235))
                    .setFontSize(36)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(40));

            document.add(new Paragraph("CERTIFICADO DE PARTICIPACIÓN")
                    .setFont(fuenteTitulo)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(4));

            document.add(new Paragraph("Otorgado a:")
                    .setFont(fuenteNormal)
                    .setFontColor(ColorConstants.GRAY)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(25));

            document.add(new Paragraph(nombreParticipante)
                    .setFont(fuenteTitulo)
                    .setFontColor(ColorConstants.BLACK)
                    .setFontSize(28)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(6));

            document.add(new Paragraph("por su participación en:")
                    .setFont(fuenteNormal)
                    .setFontColor(ColorConstants.GRAY)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(18));

            document.add(new Paragraph(nombreEvento)
                    .setFont(fuenteTitulo)
                    .setFontColor(new DeviceRgb(37, 99, 235))
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(6));

            document.add(new Paragraph(fechasEvento)
                    .setFont(fuenteNormal)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(4));

            document.add(new Paragraph("Duración: " + duracion)
                    .setFont(fuenteNormal)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(2));

            Image qrImagen = new Image(ImageDataFactory.create(rutaQr));
            qrImagen.setWidth(85);
            qrImagen.setHeight(85);
            qrImagen.setHorizontalAlignment(HorizontalAlignment.CENTER);
            qrImagen.setMarginTop(28);
            document.add(qrImagen);

            document.add(new Paragraph("Código de verificación: " + codigoUnico)
                    .setFont(fuenteNormal)
                    .setFontColor(ColorConstants.GRAY)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(6));

            // 4. Bordes — al final, pero TODAVÍA dentro del mismo try, documento sigue abierto
            PdfCanvas bordes = new PdfCanvas(pdf.getPage(1));
            bordes.setStrokeColor(new DeviceRgb(37, 99, 235))
                    .setLineWidth(3)
                    .rectangle(18, 18, w - 36, h - 36)
                    .stroke();
            bordes.setStrokeColor(new DeviceRgb(217, 119, 6))
                    .setLineWidth(1)
                    .rectangle(24, 24, w - 48, h - 48)
                    .stroke();
        }

        return archivo.toString();
    }
}