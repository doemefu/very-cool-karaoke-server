package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;

import java.util.Objects;
import java.util.UUID;

/**
 * UserTokenDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class UserTokenDTO {

    private Long id;

    private String username;

    private UUID token;

    private UserStatus status;

    public UserTokenDTO id(Long id) {
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

    public UserTokenDTO username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Get username
     *
     * @return username
     */

    @Schema(name = "username", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserTokenDTO token(UUID token) {
        this.token = token;
        return this;
    }

    /**
     * Get token
     *
     * @return token
     */
    @Valid
    @Schema(name = "token", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("token")
    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public UserTokenDTO status(UserStatus status) {
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
    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
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
        UserTokenDTO userTokenDTO = (UserTokenDTO) o;
        return Objects.equals(this.id, userTokenDTO.id) &&
                Objects.equals(this.username, userTokenDTO.username) &&
                Objects.equals(this.token, userTokenDTO.token) &&
                Objects.equals(this.status, userTokenDTO.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, token, status);
    }

    @Override
    public String toString() {
        String sb = "class UserTokenDTO {\n" +
                "    id: " + toIndentedString(id) + "\n" +
                "    username: " + toIndentedString(username) + "\n" +
                "    token: " + toIndentedString(token) + "\n" +
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

