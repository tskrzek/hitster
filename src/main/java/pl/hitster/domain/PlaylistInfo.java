package pl.hitster.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistInfo {
    private String playlistName;
    private String playlistDescription;
    private int totalTracks;
    private List<TrackInfo> tracks;
}
