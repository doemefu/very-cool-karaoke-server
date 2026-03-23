package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * VotePostDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class VotePostDTO {

    private Long songId;

    public VotePostDTO() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public VotePostDTO(Long songId) {
        this.songId = songId;
    }

    public VotePostDTO songId(Long songId) {
        this.songId = songId;
        return this;
    }

    /**
     * Get songId
     *
     * @return songId
     */
    @NotNull
    @Schema(name = "songId", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("songId")
    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VotePostDTO votePostDTO = (VotePostDTO) o;
        return Objects.equals(this.songId, votePostDTO.songId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songId);
    }

    @Override
    public String toString() {
        String sb = "class VotePostDTO {\n" +
                "    songId: " + toIndentedString(songId) + "\n" +
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

