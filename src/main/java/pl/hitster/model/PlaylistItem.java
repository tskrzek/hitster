package pl.hitster.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlaylistItem {
    @JsonProperty("added_at")
    private String addedAt;
    @JsonProperty("added_by")
    private AddedBy addedBy;
    @JsonProperty("is_local")
    private boolean isLocal;
    private Track track;
    
    @Data
    public static class AddedBy {
        private String id;
        private String href;
        private String type;
        private String uri;
        @JsonProperty("external_urls")
        private ExternalUrls externalUrls;
        
        @Data
        public static class ExternalUrls {
            private String spotify;
        }
    }
}
