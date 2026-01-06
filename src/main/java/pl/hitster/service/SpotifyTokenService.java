package pl.hitster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.hitster.model.SpotifyToken;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyTokenService {

    private final WebClient spotifyAuthWebClient;

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    private final AtomicReference<SpotifyToken> currentToken = new AtomicReference<>();

    @Scheduled(fixedRate = 45 * 60 * 1000) // co 45 minut
    public void refreshToken() {
        log.info("Scheduled token refresh starting...");
        getNewToken()
                .doOnSuccess(token -> {
                    currentToken.set(token);
                    log.info("Token refreshed successfully. Expires in: {} seconds", token.getExpiresIn());
                })
                .doOnError(error -> log.error("Failed to refresh token", error))
                .subscribe();
    }

    public Mono<SpotifyToken> getCurrentToken() {
        SpotifyToken token = currentToken.get();
        if (token == null) {
            log.info("No token available, fetching new one...");
            return getNewToken()
                    .doOnSuccess(newToken -> currentToken.set(newToken));
        }
        return Mono.just(token);
    }

    private Mono<SpotifyToken> getNewToken() {
        log.info("Requesting new access token from Spotify");
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        return spotifyAuthWebClient
                .post()
                .uri("")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(SpotifyToken.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(token -> log.info("Successfully obtained access token", token))
                .doOnError(error -> log.error("Failed to obtain access token", error));
    }
}
