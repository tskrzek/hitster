package pl.hitster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SpotifyConfig {

    @Value("${spotify.api.base-url}")
    private String spotifyApiBaseUrl;

    @Value("${spotify.auth.url}")
    private String spotifyAuthUrl;

    @Bean
    public WebClient spotifyWebClient() {
        return WebClient.builder()
                .baseUrl(spotifyApiBaseUrl)
                .build();
    }

    @Bean
    public WebClient spotifyAuthWebClient() {
        return WebClient.builder()
                .baseUrl(spotifyAuthUrl)
                .build();
    }
}
