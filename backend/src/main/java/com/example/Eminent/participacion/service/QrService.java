package com.example.Eminent.participacion.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Servicio para la generación de códigos QR para inscripciones de participantes.
 * Cada QR codifica el ID de la inscripción junto con un UUID único para
 * su verificación. Los archivos PNG se almacenan en la carpeta qr-inscripciones.
 */
@Service
public class QrService {

    private static final String CARPETA_QR = "qr-inscripciones";

    public String[] generarQr(Long inscripcionId) throws WriterException, IOException {
        String contenido = "INSCRIPCION-" + inscripcionId + "-" + UUID.randomUUID();
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 300, 300);

        Path carpeta = Path.of(CARPETA_QR);
        if (!Files.exists(carpeta)) Files.createDirectories(carpeta);

        Path archivo = carpeta.resolve("inscripcion-" + inscripcionId + ".png");
        MatrixToImageWriter.writeToPath(matrix, "PNG", archivo);

        return new String[]{archivo.toString(), contenido};
    }
}