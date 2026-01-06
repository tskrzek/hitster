package pl.hitster.model;

import lombok.Data;

import java.util.List;

@Data
public class Artist {
    private String id;
    private String name;
    private String type;
    private String uri;
    private String href;
    private List<String> genres;
    private int popularity;
    
    private ExternalUrls externalUrls;
    
    private Followers followers;
    
    private List<Image> images;
    
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
}
