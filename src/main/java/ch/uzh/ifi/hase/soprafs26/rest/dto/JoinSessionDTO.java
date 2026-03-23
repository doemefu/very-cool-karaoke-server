package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * JoinSessionDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class JoinSessionDTO {

    private String gamePin;

    public JoinSessionDTO() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public JoinSessionDTO(String gamePin) {
        this.gamePin = gamePin;
    }

    public JoinSessionDTO gamePin(String gamePin) {
        this.gamePin = gamePin;
        return this;
    }

    /**
     * Get gamePin
     *
     * @return gamePin
     */
    @NotNull
    @Schema(name = "gamePin", example = "482910", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("gamePin")
    public String getGamePin() {
        return gamePin;
    }

    public void setGamePin(String gamePin) {
        this.gamePin = gamePin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JoinSessionDTO joinSessionDTO = (JoinSessionDTO) o;
        return Objects.equals(this.gamePin, joinSessionDTO.gamePin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gamePin);
    }

    @Override
    public String toString() {
        String sb = "class JoinSessionDTO {\n" +
                "    gamePin: " + toIndentedString(gamePin) + "\n" +
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

