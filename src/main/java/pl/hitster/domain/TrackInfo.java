package pl.hitster.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackInfo {
    private String trackName;
    private int releaseYear;
    private List<String> artistNames;
    private String uri;
}
