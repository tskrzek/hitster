package pl.hitster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import pl.hitster.domain.TrackInfo;
import pl.hitster.mapper.SpotifyMapper;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistPDFService {

    private final SpotifyService spotifyService;
    private final SpotifyMapper spotifyMapper;
    private final PDFGeneratorService pdfGeneratorService;

    public Mono<byte[]> generatePlaylistPDF(String playlistId) {
        log.info("Generating PDF for playlist ID: {}", playlistId);
        
        return fetchAllTracks(playlistId)
                .map(this::generatePDFFromTracks)
                .doOnSuccess(pdfBytes -> log.info("Successfully generated PDF with {} bytes for playlist: {}", 
                        pdfBytes.length, playlistId))
                .doOnError(error -> log.error("Failed to generate PDF for playlist: {}", playlistId, error));
    }

    private Mono<List<TrackInfo>> fetchAllTracks(String playlistId) {
        return fetchTracksPage(playlistId, 0, new ArrayList<>());
    }
    
    private Mono<List<TrackInfo>> fetchTracksPage(String playlistId, int offset, List<TrackInfo> allTracks) {
        return spotifyService.getPlaylistWithTracks(playlistId, 50, offset)
                .map(spotifyMapper::playlistToPlaylistInfo)
                .flatMap(playlistInfo -> {
                    List<TrackInfo> currentTracks = playlistInfo.getTracks();
                    
                    // Sprawdź czy są puste utwory i loguj szczegóły
                    long emptyTracks = currentTracks.stream()
                            .filter(track -> track == null || track.getTrackName() == null || 
                                           track.getTrackName().trim().isEmpty() || track.getUri() == null)
                            .count();
                    
                    if (emptyTracks > 0) {
                        log.warn("Found {} empty tracks in batch (offset: {})", emptyTracks, offset);
                        // Loguj szczegóły pustych utworów
                        currentTracks.stream()
                                .filter(track -> track == null || track.getTrackName() == null || 
                                               track.getTrackName().trim().isEmpty() || track.getUri() == null)
                                .forEach(track -> log.warn("Empty track details: {}", track));
                    }
                    
                    // Loguj pierwsze kilka utworów z każdej partii
                    if (currentTracks.size() > 0) {
                        log.info("First track in batch (offset: {}): name='{}', uri='{}'", 
                                offset, 
                                currentTracks.get(0).getTrackName(), 
                                currentTracks.get(0).getUri());
                    }
                    
                    allTracks.addAll(currentTracks);
                    
                    log.info("Fetched {} tracks (offset: {}), total so far: {}, empty tracks: {}", 
                            currentTracks.size(), offset, allTracks.size(), emptyTracks);
                    
                    // Jeśli otrzymaliśmy mniej niż 50 utworów, to koniec
                    if (currentTracks.size() < 50) {
                        log.info("Reached end of playlist. Total tracks fetched: {}", allTracks.size());
                        return Mono.just(allTracks);
                    }
                    
                    // Pobierz następną stronę
                    return fetchTracksPage(playlistId, offset + 50, allTracks);
                });
    }
    
    private byte[] generatePDFFromTracks(List<TrackInfo> tracks) {
        log.info("Starting PDF generation with {} total tracks", tracks.size());
        
        // Filtruj puste utwory
        List<TrackInfo> validTracks = tracks.stream()
                .filter(track -> track != null && track.getTrackName() != null && 
                               !track.getTrackName().trim().isEmpty() && track.getUri() != null)
                .collect(java.util.stream.Collectors.toList());
        
        long filteredCount = tracks.size() - validTracks.size();
        if (filteredCount > 0) {
            log.warn("Filtered out {} empty tracks, generating PDF for {} valid tracks", 
                    filteredCount, validTracks.size());
        }
        
        // Sprawdź duplikaty
        long uniqueTracks = validTracks.stream()
                .map(TrackInfo::getUri)
                .distinct()
                .count();
        
        if (uniqueTracks != validTracks.size()) {
            log.warn("Found {} duplicate tracks! Total: {}, Unique: {}", 
                    validTracks.size() - uniqueTracks, validTracks.size(), uniqueTracks);
        }
        
        // Loguj pierwsze kilka utworów
        log.info("First 5 tracks for PDF generation:");
        validTracks.stream().limit(5).forEach(track -> 
                log.info("  - {} by {} (URI: {})", 
                        track.getTrackName(), 
                        String.join(", ", track.getArtistNames()), 
                        track.getUri()));
        
        log.info("Generating PDF for {} tracks", validTracks.size());
        return pdfGeneratorService.generatePlaylistPDF(validTracks);
    }
}

