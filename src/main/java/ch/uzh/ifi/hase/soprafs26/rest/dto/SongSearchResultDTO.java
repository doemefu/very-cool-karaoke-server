package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;

import java.util.Objects;

/**
 * SongSearchResultDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class SongSearchResultDTO {

    private String spotifyId = null;

    private String title;

    private String artist;

    private String albumName;

    private String albumArt = null;

    private Integer durationMs;

    private Integer durationSeconds;

    private Boolean lyricsAvailable;

    public SongSearchResultDTO spotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
        return this;
    }

    /**
     * Get spotifyId
     *
     * @return spotifyId
     */

    @Schema(name = "spotifyId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("spotifyId")
    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public SongSearchResultDTO title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get title
     *
     * @return title
     */

    @Schema(name = "title", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SongSearchResultDTO artist(String artist) {
        this.artist = artist;
        return this;
    }

    /**
     * Get artist
     *
     * @return artist
     */

    @Schema(name = "artist", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("artist")
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public SongSearchResultDTO albumArt(String albumArt) {
        this.albumArt = albumArt;
        return this;
    }

    @Schema(name = "albumArt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("albumArt")
    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    @Schema(name = "albumName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("albumName")
    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public SongSearchResultDTO durationMs(Integer durationMs) {
        this.durationMs = durationMs;
        return this;
    }

    @Schema(name = "durationMs", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("durationMs")
    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public SongSearchResultDTO durationSeconds(Integer durationSeconds) {
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

    public SongSearchResultDTO lyricsAvailable(Boolean lyricsAvailable) {
        this.lyricsAvailable = lyricsAvailable;
        return this;
    }

    /**
     * Get lyricsAvailable
     *
     * @return lyricsAvailable
     */

    @Schema(name = "lyricsAvailable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("lyricsAvailable")
    public Boolean getLyricsAvailable() {
        return lyricsAvailable;
    }

    public void setLyricsAvailable(Boolean lyricsAvailable) {
        this.lyricsAvailable = lyricsAvailable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SongSearchResultDTO songSearchResultDTO = (SongSearchResultDTO) o;
        return Objects.equals(this.spotifyId, songSearchResultDTO.spotifyId) &&
                Objects.equals(this.title, songSearchResultDTO.title) &&
                Objects.equals(this.artist, songSearchResultDTO.artist) &&
                Objects.equals(this.albumArt, songSearchResultDTO.albumArt) &&
                Objects.equals(this.albumName, songSearchResultDTO.albumName) &&
                Objects.equals(this.durationMs, songSearchResultDTO.durationMs) &&
                Objects.equals(this.durationSeconds, songSearchResultDTO.durationSeconds) &&
                Objects.equals(this.lyricsAvailable, songSearchResultDTO.lyricsAvailable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spotifyId, title, artist, albumArt, albumName, durationMs, durationSeconds, lyricsAvailable);
    }

    @Override
    public String toString() {
        String sb = "class SongSearchResultDTO {\n" +
                "    spotifyId: " + toIndentedString(spotifyId) + "\n" +
                "    title: " + toIndentedString(title) + "\n" +
                "    artist: " + toIndentedString(artist) + "\n" +
                "    albumName: " + toIndentedString(albumName) + "\n" +
                "    albumArt: " + toIndentedString(albumArt) + "\n" +
                "    durationMs: " + toIndentedString(durationMs) + "\n" +
                "    durationSeconds: " + toIndentedString(durationSeconds) + "\n" +
                "    lyricsAvailable: " + toIndentedString(lyricsAvailable) + "\n" +
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

