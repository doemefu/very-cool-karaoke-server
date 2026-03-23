package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
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
 * SessionGetDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class SessionGetDTO {

    private Long id;

    private String name;

    private String description;

    private String gamePin;

    private SessionStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime createdAt;

    private UserGetDTO admin;

    @Valid
    private List<@Valid UserGetDTO> participants = new ArrayList<>();

    public SessionGetDTO id(Long id) {
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

    public SessionGetDTO name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     */

    @Schema(name = "name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SessionGetDTO description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get description
     *
     * @return description
     */

    @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SessionGetDTO gamePin(String gamePin) {
        this.gamePin = gamePin;
        return this;
    }

    /**
     * Get gamePin
     *
     * @return gamePin
     */

    @Schema(name = "gamePin", example = "482910", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("gamePin")
    public String getGamePin() {
        return gamePin;
    }

    public void setGamePin(String gamePin) {
        this.gamePin = gamePin;
    }

    public SessionGetDTO status(SessionStatus status) {
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
    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public SessionGetDTO createdAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Get createdAt
     *
     * @return createdAt
     */
    @Valid
    @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("createdAt")
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public SessionGetDTO admin(UserGetDTO admin) {
        this.admin = admin;
        return this;
    }

    /**
     * Get admin
     *
     * @return admin
     */
    @Valid
    @Schema(name = "admin", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("admin")
    public UserGetDTO getAdmin() {
        return admin;
    }

    public void setAdmin(UserGetDTO admin) {
        this.admin = admin;
    }

    public SessionGetDTO participants(List<@Valid UserGetDTO> participants) {
        this.participants = participants;
        return this;
    }

    public SessionGetDTO addParticipantsItem(UserGetDTO participantsItem) {
        if (this.participants == null) {
            this.participants = new ArrayList<>();
        }
        this.participants.add(participantsItem);
        return this;
    }

    /**
     * Get participants
     *
     * @return participants
     */
    @Valid
    @Schema(name = "participants", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("participants")
    public List<@Valid UserGetDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<@Valid UserGetDTO> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionGetDTO sessionGetDTO = (SessionGetDTO) o;
        return Objects.equals(this.id, sessionGetDTO.id) &&
                Objects.equals(this.name, sessionGetDTO.name) &&
                Objects.equals(this.description, sessionGetDTO.description) &&
                Objects.equals(this.gamePin, sessionGetDTO.gamePin) &&
                Objects.equals(this.status, sessionGetDTO.status) &&
                Objects.equals(this.createdAt, sessionGetDTO.createdAt) &&
                Objects.equals(this.admin, sessionGetDTO.admin) &&
                Objects.equals(this.participants, sessionGetDTO.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, gamePin, status, createdAt, admin, participants);
    }

    @Override
    public String toString() {
        String sb = "class SessionGetDTO {\n" +
                "    id: " + toIndentedString(id) + "\n" +
                "    name: " + toIndentedString(name) + "\n" +
                "    description: " + toIndentedString(description) + "\n" +
                "    gamePin: " + toIndentedString(gamePin) + "\n" +
                "    status: " + toIndentedString(status) + "\n" +
                "    createdAt: " + toIndentedString(createdAt) + "\n" +
                "    admin: " + toIndentedString(admin) + "\n" +
                "    participants: " + toIndentedString(participants) + "\n" +
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

