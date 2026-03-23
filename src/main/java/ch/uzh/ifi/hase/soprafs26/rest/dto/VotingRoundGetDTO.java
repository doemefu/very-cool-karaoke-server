package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.VotingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * VotingRoundGetDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class VotingRoundGetDTO {

    private Long id;

    private Integer roundNumber;

    private VotingStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime startedAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime endsAt;

    @Valid
    private List<@Valid SongGetDTO> candidates = new ArrayList<>();

    public VotingRoundGetDTO id(Long id) {
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

    public VotingRoundGetDTO roundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
        return this;
    }

    /**
     * Get roundNumber
     *
     * @return roundNumber
     */

    @Schema(name = "roundNumber", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("roundNumber")
    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public VotingRoundGetDTO status(VotingStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     */
    @Valid
    @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("status")
    public VotingStatus getStatus() {
        return status;
    }

    public void setStatus(VotingStatus status) {
        this.status = status;
    }

    public VotingRoundGetDTO startedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
        return this;
    }

    /**
     * Get startedAt
     *
     * @return startedAt
     */
    @Valid
    @Schema(name = "startedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("startedAt")
    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public VotingRoundGetDTO endsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
        return this;
    }

    /**
     * Get endsAt
     *
     * @return endsAt
     */
    @Valid
    @Schema(name = "endsAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("endsAt")
    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public VotingRoundGetDTO candidates(List<@Valid SongGetDTO> candidates) {
        this.candidates = candidates;
        return this;
    }

    public VotingRoundGetDTO addCandidatesItem(SongGetDTO candidatesItem) {
        if (this.candidates == null) {
            this.candidates = new ArrayList<>();
        }
        this.candidates.add(candidatesItem);
        return this;
    }

    /**
     * Sorted descending by currentVoteCount
     *
     * @return candidates
     */
    @Valid
    @Schema(name = "candidates", description = "Sorted descending by currentVoteCount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("candidates")
    public List<@Valid SongGetDTO> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<@Valid SongGetDTO> candidates) {
        this.candidates = candidates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VotingRoundGetDTO votingRoundGetDTO = (VotingRoundGetDTO) o;
        return Objects.equals(this.id, votingRoundGetDTO.id) &&
                Objects.equals(this.roundNumber, votingRoundGetDTO.roundNumber) &&
                Objects.equals(this.status, votingRoundGetDTO.status) &&
                Objects.equals(this.startedAt, votingRoundGetDTO.startedAt) &&
                Objects.equals(this.endsAt, votingRoundGetDTO.endsAt) &&
                Objects.equals(this.candidates, votingRoundGetDTO.candidates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roundNumber, status, startedAt, endsAt, candidates);
    }

    @Override
    public String toString() {
        String sb = "class VotingRoundGetDTO {\n" +
                "    id: " + toIndentedString(id) + "\n" +
                "    roundNumber: " + toIndentedString(roundNumber) + "\n" +
                "    status: " + toIndentedString(status) + "\n" +
                "    startedAt: " + toIndentedString(startedAt) + "\n" +
                "    endsAt: " + toIndentedString(endsAt) + "\n" +
                "    candidates: " + toIndentedString(candidates) + "\n" +
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

