package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * SessionPutDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class SessionPutDTO {

    private SessionStatus status;

    public SessionPutDTO() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public SessionPutDTO(SessionStatus status) {
        this.status = status;
    }

    public SessionPutDTO status(SessionStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     */
    @NotNull
    @Valid
    @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("status")
    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SessionPutDTO sessionPutDTO = (SessionPutDTO) o;
        return Objects.equals(this.status, sessionPutDTO.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @Override
    public String toString() {
        String sb = "class SessionPutDTO {\n" +
                "    status: " + toIndentedString(status) + "\n" +
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

