package pl.hitster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.hitster.domain.PlaylistInfo;
import pl.hitster.mapper.SpotifyMapper;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistProcessorService {

    private final SpotifyService spotifyService;
    private final SpotifyMapper spotifyMapper;

    public Mono<PlaylistInfo> getProcessedPlaylist(String playlistId, int limit, int offset) {
        log.info("Processing playlist with ID: {}, limit: {}, offset: {}", playlistId, limit, offset);
        
        return spotifyService.getPlaylistWithTracks(playlistId, limit, offset)
                .map(spotifyMapper::playlistToPlaylistInfo)
                .doOnSuccess(playlistInfo -> log.info("Successfully processed playlist: {} with {} tracks", 
                        playlistInfo.getPlaylistName(), playlistInfo.getTracks().size()))
                .doOnError(error -> log.error("Failed to process playlist", error));
    }


}
