package pl.hitster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.hitster.model.Playlist;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyService {

    private final WebClient spotifyWebClient;
    private final SpotifyTokenService tokenService;

    public Mono<Playlist> getPlaylist(String playlistId) {
        return tokenService.getCurrentToken()
                .flatMap(token -> {
                    log.info("Fetching playlist with ID: {}", playlistId);
                    return spotifyWebClient
                            .get()
                            .uri("/playlists/{id}", playlistId)
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(Playlist.class)
                            .timeout(Duration.ofSeconds(10))
                            .doOnSuccess(playlist -> log.info("Successfully fetched playlist: {}", playlist.getName()))
                            .doOnError(error -> log.error("Failed to fetch playlist with ID: {}", playlistId, error));
                });
    }

    public Mono<String> getPlaylistTracks(String playlistId, int limit, int offset) {
        return tokenService.getCurrentToken()
                .flatMap(token -> {
                    log.info("Fetching tracks for playlist ID: {}", playlistId);
                    return spotifyWebClient
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/playlists/{id}/tracks")
                                    .queryParam("limit", limit)
                                    .queryParam("offset", offset)
                                    .build(playlistId))
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(String.class)
                            .timeout(Duration.ofSeconds(60))
                            .doOnSuccess(result -> log.info("Successfully fetched playlist tracks"))
                            .doOnError(error -> log.error("Failed to fetch playlist tracks", error));
                });
    }

    public Mono<Playlist> getPlaylistWithTracks(String playlistId, int limit, int offset) {
        return tokenService.getCurrentToken()
                .flatMap(token -> {
                    log.info("Fetching playlist with tracks for ID: {}", playlistId);
                    return spotifyWebClient
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/playlists/{id}/tracks")
                                    .queryParam("limit", limit)
                                    .queryParam("offset", offset)
                                    .build(playlistId))
                            .header("Authorization", "Bearer " + token.getAccessToken())
                            .retrieve()
                            .bodyToMono(Playlist.class)
                            .timeout(Duration.ofSeconds(60))
                            .doOnSuccess(result -> log.info("Successfully fetched playlist with tracks"))
                            .doOnError(error -> log.error("Failed to fetch playlist with tracks", error));
                });
    }
}
