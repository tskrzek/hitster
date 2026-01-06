package pl.hitster.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.hitster.domain.PlaylistInfo;
import pl.hitster.domain.TrackInfo;
import pl.hitster.model.Playlist;
import pl.hitster.model.PlaylistItem;
import pl.hitster.model.Track;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SpotifyMapper {

    @Mapping(target = "playlistName", source = "name")
    @Mapping(target = "playlistDescription", source = "description")
    @Mapping(target = "totalTracks", source = "total")
    @Mapping(target = "tracks", source = "items", qualifiedByName = "mapPlaylistItemsToTrackInfos")
    PlaylistInfo playlistToPlaylistInfo(Playlist playlist);

    @Named("mapPlaylistItemsToTrackInfos")
    default List<TrackInfo> mapPlaylistItemsToTrackInfos(List<PlaylistItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(PlaylistItem::getTrack)
                .filter(Objects::nonNull)
                .map(this::trackToTrackInfo)
                .collect(Collectors.toList());
    }

    @Mapping(target = "trackName", source = "name", qualifiedByName = "cleanTrackName")
    @Mapping(target = "releaseYear", source = "album.releaseDate", qualifiedByName = "extractYear")
    @Mapping(target = "artistNames", source = "artists", qualifiedByName = "mapArtistsToNames")
    @Mapping(target = "uri", source = "uri")
    TrackInfo trackToTrackInfo(Track track);

    @Named("extractYear")
    default int extractYear(String releaseDate) {
        if (releaseDate == null || releaseDate.isEmpty()) {
            return 0;
        }
        try {
            String[] dateParts = releaseDate.split("-");
            return Integer.parseInt(dateParts[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Named("cleanTrackName")
    default String cleanTrackName(String trackName) {
        if (trackName == null || trackName.trim().isEmpty()) {
            return trackName;
        }
        
        // // Sprawdź czy tytuł zawiera pauzę
        // if (trackName.contains(" - ")) {
        //     String[] parts = trackName.split(" - ", 2);
        //     if (parts.length == 2) {
        //         String secondPart = parts[1].toLowerCase();
        //         // Sprawdź czy druga część zawiera "remaster" lub "remastered"
        //         if (secondPart.contains("remaster")) {
        //             return parts[0].trim() + " [REMASTER]";
        //         }
        //     }
        // }
        
        return trackName;
    }

    @Named("mapArtistsToNames")
    default List<String> mapArtistsToNames(List<Track.Artist> artists) {
        if (artists == null) {
            return List.of();
        }
        return artists.stream()
                .map(Track.Artist::getName)
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.toList());
    }
}
