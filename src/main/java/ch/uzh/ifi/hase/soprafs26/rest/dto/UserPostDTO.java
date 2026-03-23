package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * UserPostDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class UserPostDTO {

    private String username;

    private String password;

    public UserPostDTO() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public UserPostDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserPostDTO username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Get username
     *
     * @return username
     */
    @NotNull
    @Schema(name = "username", example = "dominic42", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserPostDTO password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get password
     *
     * @return password
     */
    @NotNull
    @Schema(name = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserPostDTO userPostDTO = (UserPostDTO) o;
        return Objects.equals(this.username, userPostDTO.username) &&
                Objects.equals(this.password, userPostDTO.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    @Override
    public String toString() {
        String sb = "class UserPostDTO {\n" +
                "    username: " + toIndentedString(username) + "\n" +
                "    password: " + "*" + "\n" +
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

