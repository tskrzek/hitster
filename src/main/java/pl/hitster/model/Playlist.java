package pl.hitster.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Playlist {
    private String id;
    private String name;
    private String description;
    private String href;
    private String uri;
    private String type;
    private boolean collaborative;
    @JsonProperty("public")
    private boolean publicPlaylist;
    @JsonProperty("snapshot_id")
    private String snapshotId;
    private int total;
    private String next;
    private String previous;
    private int limit;
    private int offset;
    
    @JsonProperty("external_urls")
    private ExternalUrls externalUrls;
    private Followers followers;
    private List<Image> images;
    private Owner owner;
    private List<PlaylistItem> items;
    
    @Data
    public static class ExternalUrls {
        private String spotify;
    }
    
    @Data
    public static class Followers {
        private String href;
        private int total;
    }
    
    @Data
    public static class Image {
        private String url;
        private int height;
        private int width;
    }
    
    @Data
    public static class Owner {
        private String id;
        @JsonProperty("display_name")
        private String displayName;
        private String href;
        private String type;
        private String uri;
        @JsonProperty("external_urls")
        private ExternalUrls externalUrls;
        private Followers followers;
    }
}
