package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * SongPostDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class SongPostDTO {

    private String spotifyId = null;

    private String title;

    private String artist;

    private String albumArt = null;

    private String albumName = null;

    private Integer durationMs;

    private Integer durationSeconds;

    public SongPostDTO() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public SongPostDTO(String spotifyId, String title, String artist, String albumName) {
        this.spotifyId = spotifyId;
        this.title = title;
        this.artist = artist;
        this.albumName = albumName;
    }

    public SongPostDTO spotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
        return this;
    }

    /**
     * Get spotifyId
     *
     * @return spotifyId
     */

    @NotNull
    @Schema(name = "spotifyId", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("spotifyId")
    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public SongPostDTO title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get title
     *
     * @return title
     */
    @NotNull
    @Schema(name = "title", example = "Dancing Queen", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SongPostDTO artist(String artist) {
        this.artist = artist;
        return this;
    }

    /**
     * Get artist
     *
     * @return artist
     */
    @NotNull
    @Schema(name = "artist", example = "ABBA", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("artist")
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Schema(name = "albumArt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("albumArt")
    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public SongPostDTO albumName(String albumName) {
        this.albumName = albumName;
        return this;
    }

    @Schema(name = "albumName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("albumName")
    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    @NotNull
    @Schema(name = "durationMs", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("durationMs")
    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public SongPostDTO durationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
        return this;
    }

    @Schema(name = "durationSeconds", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("durationSeconds")
    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SongPostDTO songPostDTO = (SongPostDTO) o;
        return Objects.equals(this.spotifyId, songPostDTO.spotifyId) &&
                Objects.equals(this.title, songPostDTO.title) &&
                Objects.equals(this.artist, songPostDTO.artist) &&
                Objects.equals(this.albumArt, songPostDTO.albumArt) &&
                Objects.equals(this.albumName, songPostDTO.albumName) &&
                Objects.equals(this.durationMs, songPostDTO.durationMs) &&
                Objects.equals(this.durationSeconds, songPostDTO.durationSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spotifyId, title, artist, albumArt, albumName, durationMs, durationSeconds);
    }

    @Override
    public String toString() {
        String sb = "class SongPostDTO {\n" +
                "    spotifyId: " + toIndentedString(spotifyId) + "\n" +
                "    title: " + toIndentedString(title) + "\n" +
                "    artist: " + toIndentedString(artist) + "\n" +
                "    albumArt: " + toIndentedString(albumArt) + "\n" +
                "    albumName: " + toIndentedString(albumName) + "\n" +
                "    durationMs: " + toIndentedString(durationMs) + "\n" +
                "    durationSeconds: " + toIndentedString(durationSeconds) + "\n" +
                "}";
        return sb;
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
