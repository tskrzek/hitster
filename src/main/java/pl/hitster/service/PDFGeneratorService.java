package pl.hitster.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.hitster.domain.TrackInfo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFGeneratorService {

    private final QRCodeService qrCodeService;

    private static final int TRACKS_PER_PAGE = 10; // 10 utworów na stronę (2 kolumny x 5 rzędów)
    private static final float TILE_SIZE = 145f; // Rozmiar kwadratowego kafelka w punktach
    private static final float QR_SIZE = 120f; // Rozmiar QR kodu w punktach
    private static final float MARGIN = 5f; // Mniejsze marginesy

    public byte[] generatePlaylistPDF(List<TrackInfo> tracks) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDocument = new PdfDocument(writer);
             Document document = new Document(pdfDocument, PageSize.A4)) {

            document.setMargins(10f, 15f, 10f, 7f); // top, right, bottom, left
            
            // Inicjalizacja czcionki obsługującej polskie znaki
            PdfFont font = PdfFontFactory.createFont("Helvetica", PdfEncodings.CP1250);

            // Oblicz ile utworów będzie na każdej stronie (2 kolumny x 5 rzędów = 10 utworów)
            int tracksPerPage = TRACKS_PER_PAGE;
            int totalPages = (int) Math.ceil((double) tracks.size() / tracksPerPage);
            
            log.info("Generating PDF with {} pages for {} tracks ({} tracks per page - 2 columns x 5 rows)", 
                    totalPages, tracks.size(), tracksPerPage);

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                int startIndex = pageIndex * tracksPerPage;
                int endIndex = Math.min(startIndex + tracksPerPage, tracks.size());
                List<TrackInfo> pageTracks = tracks.subList(startIndex, endIndex);
                
                log.info("Page {}: processing tracks {} to {} ({} tracks)", 
                        pageIndex + 1, startIndex, endIndex - 1, pageTracks.size());

                // Strona z dwoma kolumnami (2x5 = 10 utworów)
                addTwoColumnPage(document, pageTracks, font);
                
                // Przejdź do następnej strony (oprócz ostatniej)
                if (pageIndex < totalPages - 1) {
                    document.add(new com.itextpdf.layout.element.AreaBreak());
                }
            }

            document.close();
            log.info("Successfully generated PDF with {} bytes", outputStream.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addTwoColumnPage(Document document, List<TrackInfo> tracks, PdfFont font) {
        log.info("Adding two column page with {} tracks", tracks.size());
        
        // Podziel utwory na dwie kolumny (po 5 w każdej)
        int halfSize = (tracks.size() + 1) / 2; // Zaokrąglij w górę
        
        // Lewa kolumna (pierwsze 5 utworów)
        List<TrackInfo> leftColumn = tracks.subList(0, Math.min(halfSize, tracks.size()));
        
        // Prawa kolumna (pozostałe utwory)
        List<TrackInfo> rightColumn = tracks.subList(halfSize, tracks.size());
        
        // Tabela z 4 kolumnami (2 pary kafelków obok siebie)
        Table twoColumnTable = new Table(UnitValue.createPointArray(new float[]{TILE_SIZE, TILE_SIZE, TILE_SIZE, TILE_SIZE}))
                .setWidth(UnitValue.createPointValue(TILE_SIZE * 4))
                .setFixedLayout();
        
        // Dodaj utwory z obu kolumn
        int maxRows = Math.max(leftColumn.size(), rightColumn.size());
        for (int i = 0; i < maxRows; i++) {
            // Lewa kolumna - para kafelków
            if (i < leftColumn.size()) {
                addPairToTable(twoColumnTable, leftColumn.get(i), font);
            } else {
                // Puste komórki jeśli brak utworu w lewej kolumnie
                twoColumnTable.addCell(new com.itextpdf.layout.element.Cell()
                        .setWidth(TILE_SIZE)
                        .setHeight(TILE_SIZE)
                        .setPadding(0)
                        .setMinHeight(TILE_SIZE));
                twoColumnTable.addCell(new com.itextpdf.layout.element.Cell()
                        .setWidth(TILE_SIZE)
                        .setHeight(TILE_SIZE)
                        .setPadding(0)
                        .setMinHeight(TILE_SIZE));
            }
            
            // Prawa kolumna - para kafelków
            if (i < rightColumn.size()) {
                addPairToTable(twoColumnTable, rightColumn.get(i), font);
            } else {
                // Puste komórki jeśli brak utworu w prawej kolumnie
                twoColumnTable.addCell(new com.itextpdf.layout.element.Cell()
                        .setWidth(TILE_SIZE)
                        .setHeight(TILE_SIZE)
                        .setPadding(0)
                        .setMinHeight(TILE_SIZE));
                twoColumnTable.addCell(new com.itextpdf.layout.element.Cell()
                        .setWidth(TILE_SIZE)
                        .setHeight(TILE_SIZE)
                        .setPadding(0)
                        .setMinHeight(TILE_SIZE));
            }
        }
        
        document.add(twoColumnTable);
    }
    
    private void addPairToTable(Table table, TrackInfo track, PdfFont font) {
        // Sprawdź czy track ma wszystkie potrzebne dane
        if (track == null || track.getUri() == null || track.getTrackName() == null || 
            track.getTrackName().trim().isEmpty()) {
            log.warn("Skipping track with missing data: {}", track);
            // Dodaj puste komórki zamiast błędnych danych
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .setWidth(TILE_SIZE)
                    .setHeight(TILE_SIZE)
                    .setPadding(0)
                    .setMinHeight(TILE_SIZE));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .setWidth(TILE_SIZE)
                    .setHeight(TILE_SIZE)
                    .setPadding(0)
                    .setMinHeight(TILE_SIZE));
            return;
        }
        
        log.debug("Processing track: '{}' with URI: '{}'", track.getTrackName(), track.getUri());
        
        // Lewy kafelek - QR kod
        BufferedImage qrImage = qrCodeService.generateQRCode(track.getUri());
        try {
            byte[] imageBytes = bufferedImageToByteArray(qrImage);
            Image pdfImage = new Image(ImageDataFactory.create(imageBytes));
            pdfImage.scaleToFit(QR_SIZE, QR_SIZE);
            pdfImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
            
            com.itextpdf.layout.element.Cell qrCell = new com.itextpdf.layout.element.Cell()
                    .add(pdfImage)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8)
                    .setWidth(TILE_SIZE)
                    .setHeight(TILE_SIZE)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                    .setMinHeight(TILE_SIZE);
            
            table.addCell(qrCell);
            log.debug("Successfully added QR code for track: {}", track.getTrackName());
        } catch (Exception e) {
            log.error("Error adding QR code for track: {}", track.getTrackName(), e);
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("QR Error").setFont(font))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8)
                    .setWidth(TILE_SIZE)
                    .setHeight(TILE_SIZE)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                    .setMinHeight(TILE_SIZE));
        }

        // Prawy kafelek - informacje o utworze
        com.itextpdf.layout.element.Cell trackInfoCell = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(track.getTrackName()).setFont(font).setFontSize(11).setBold())
                .add(new Paragraph("").setFont(font))
                .add(new Paragraph(String.valueOf(track.getReleaseYear())).setFont(font).setFontSize(16).setBold())
                .add(new Paragraph("").setFont(font))
                .add(new Paragraph(String.join(", ", track.getArtistNames())).setFont(font).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8)
                .setWidth(TILE_SIZE)
                .setHeight(TILE_SIZE)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                .setMinHeight(TILE_SIZE);
        
        table.addCell(trackInfoCell);
    }

    private byte[] bufferedImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
