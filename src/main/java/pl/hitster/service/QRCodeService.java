package pl.hitster.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class QRCodeService {

    private static final int QR_SIZE = 150; // Rozmiar QR kodu w pikselach

    public BufferedImage generateQRCode(String uri) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(uri, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

            BufferedImage qrImage = new BufferedImage(QR_SIZE, QR_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = qrImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, QR_SIZE, QR_SIZE);
            graphics.setColor(Color.BLACK);

            for (int x = 0; x < QR_SIZE; x++) {
                for (int y = 0; y < QR_SIZE; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }

            graphics.dispose();
            log.info("Generated QR code for URI: {}", uri);
            return qrImage;

        } catch (WriterException e) {
            log.error("Error generating QR code for URI: {}", uri, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
