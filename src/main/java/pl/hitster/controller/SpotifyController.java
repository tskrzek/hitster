package pl.hitster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.hitster.domain.PlaylistInfo;
import pl.hitster.model.Playlist;
import pl.hitster.service.PlaylistPDFService;
import pl.hitster.service.PlaylistProcessorService;
import pl.hitster.service.SpotifyService;
import pl.hitster.service.SpotifyTokenService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
@Slf4j
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final SpotifyTokenService tokenService;
    private final PlaylistProcessorService playlistProcessorService;
    private final PlaylistPDFService playlistPDFService;

    @GetMapping("/token")
    public Mono<ResponseEntity<String>> getToken() {
        log.info("GET request for access token");
        return tokenService.getCurrentToken()
                .map(token -> ResponseEntity.ok(token.getAccessToken()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping("/playlist/{id}")
    public Mono<ResponseEntity<Playlist>> getPlaylist(@PathVariable String id) {
        log.info("GET request for playlist with ID: {}", id);
        return spotifyService.getPlaylist(id)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @GetMapping("/playlist/{id}/tracks")
    public Mono<ResponseEntity<String>> getPlaylistTracks(
            @PathVariable String id,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        log.info("GET request for playlist tracks with ID: {}, limit: {}, offset: {}", id, limit, offset);
        return spotifyService.getPlaylistTracks(id, limit, offset)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping("/playlist/{id}/processed")
    public Mono<ResponseEntity<PlaylistInfo>> getProcessedPlaylist(
            @PathVariable String id,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        log.info("GET request for processed playlist with ID: {}, limit: {}, offset: {}", id, limit, offset);
        return playlistProcessorService.getProcessedPlaylist(id, limit, offset)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping("/playlist/{id}/pdf")
    public Mono<ResponseEntity<byte[]>> generatePlaylistPDF(@PathVariable String id) {
        log.info("GET request for PDF generation for playlist ID: {}", id);
        return playlistPDFService.generatePlaylistPDF(id)
                .map(pdfBytes -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", "playlist_" + id + ".pdf");
                    headers.setContentLength(pdfBytes.length);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(pdfBytes);
                })
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
