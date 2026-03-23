package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;

import java.util.Objects;

/**
 * SongGetDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class SongGetDTO {

    private Long id;

    private String spotifyId = null;

    private String geniusId = null;

    private String title;

    private String artist;

    private String lyrics = null;

    private Integer currentVoteCount;

    private Boolean performed;

    private UserGetDTO addedBy;

    public SongGetDTO id(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     */

    @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SongGetDTO spotifyId(String spotifyId) {
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

    public SongGetDTO geniusId(String geniusId) {
        this.geniusId = geniusId;
        return this;
    }

    /**
     * Get geniusId
     *
     * @return geniusId
     */

    @Schema(name = "geniusId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("geniusId")
    public String getGeniusId() {
        return geniusId;
    }

    public void setGeniusId(String geniusId) {
        this.geniusId = geniusId;
    }

    public SongGetDTO title(String title) {
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

    public SongGetDTO artist(String artist) {
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

    public SongGetDTO lyrics(String lyrics) {
        this.lyrics = lyrics;
        return this;
    }

    /**
     * Get lyrics
     *
     * @return lyrics
     */

    @Schema(name = "lyrics", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("lyrics")
    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public SongGetDTO currentVoteCount(Integer currentVoteCount) {
        this.currentVoteCount = currentVoteCount;
        return this;
    }

    /**
     * Get currentVoteCount
     *
     * @return currentVoteCount
     */

    @Schema(name = "currentVoteCount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("currentVoteCount")
    public Integer getCurrentVoteCount() {
        return currentVoteCount;
    }

    public void setCurrentVoteCount(Integer currentVoteCount) {
        this.currentVoteCount = currentVoteCount;
    }

    public SongGetDTO performed(Boolean performed) {
        this.performed = performed;
        return this;
    }

    /**
     * Get performed
     *
     * @return performed
     */

    @Schema(name = "performed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("performed")
    public Boolean getPerformed() {
        return performed;
    }

    public void setPerformed(Boolean performed) {
        this.performed = performed;
    }

    public SongGetDTO addedBy(UserGetDTO addedBy) {
        this.addedBy = addedBy;
        return this;
    }

    /**
     * Get addedBy
     *
     * @return addedBy
     */
    @Valid
    @Schema(name = "addedBy", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("addedBy")
    public UserGetDTO getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(UserGetDTO addedBy) {
        this.addedBy = addedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SongGetDTO songGetDTO = (SongGetDTO) o;
        return Objects.equals(this.id, songGetDTO.id) &&
                Objects.equals(this.spotifyId, songGetDTO.spotifyId) &&
                Objects.equals(this.geniusId, songGetDTO.geniusId) &&
                Objects.equals(this.title, songGetDTO.title) &&
                Objects.equals(this.artist, songGetDTO.artist) &&
                Objects.equals(this.lyrics, songGetDTO.lyrics) &&
                Objects.equals(this.currentVoteCount, songGetDTO.currentVoteCount) &&
                Objects.equals(this.performed, songGetDTO.performed) &&
                Objects.equals(this.addedBy, songGetDTO.addedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spotifyId, geniusId, title, artist, lyrics, currentVoteCount, performed, addedBy);
    }

    @Override
    public String toString() {
        String sb = "class SongGetDTO {\n" +
                "    id: " + toIndentedString(id) + "\n" +
                "    spotifyId: " + toIndentedString(spotifyId) + "\n" +
                "    geniusId: " + toIndentedString(geniusId) + "\n" +
                "    title: " + toIndentedString(title) + "\n" +
                "    artist: " + toIndentedString(artist) + "\n" +
                "    lyrics: " + toIndentedString(lyrics) + "\n" +
                "    currentVoteCount: " + toIndentedString(currentVoteCount) + "\n" +
                "    performed: " + toIndentedString(performed) + "\n" +
                "    addedBy: " + toIndentedString(addedBy) + "\n" +
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

