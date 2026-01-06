package pl.hitster.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Track {
    private String id;
    private String name;
    private String href;
    private String uri;
    private String type;
    @JsonProperty("duration_ms")
    private int durationMs;
    private int popularity;
    @JsonProperty("is_playable")
    private boolean isPlayable;
    @JsonProperty("is_local")
    private boolean isLocal;
    @JsonProperty("preview_url")
    private String previewUrl;
    
    private Album album;
    private List<Artist> artists;
    
    @Data
    public static class Album {
        private String id;
        private String name;
        @JsonProperty("album_type")
        private String albumType;
        @JsonProperty("total_tracks")
        private int totalTracks;
        @JsonProperty("release_date")
        private String releaseDate;
        @JsonProperty("release_date_precision")
        private String releaseDatePrecision;
        private String href;
        private String uri;
        private String type;
        
        @Data
        public static class Image {
            private String url;
            private int height;
            private int width;
        }
    }
    
    @Data
    public static class Artist {
        private String id;
        private String name;
        private String href;
        private String uri;
        private String type;
    }
}
