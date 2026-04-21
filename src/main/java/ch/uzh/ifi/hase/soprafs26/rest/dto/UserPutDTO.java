package ch.uzh.ifi.hase.soprafs26.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;

import java.util.Objects;

/**
 * UserPutDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public class UserPutDTO {

    private String newPassword;

    private String oldPassword;

    public UserPutDTO newPassword(String newPassword) {
        this.newPassword = newPassword;
        return this;
    }

    /**
     * Get newPassword
     *
     * @return newPassword
     */

    @Schema(name = "newPassword", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("newPassword")
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public UserPutDTO oldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
        return this;
    }

    /**
     * Get oldPassword
     *
     * @return oldPassword
     */

    @Schema(name = "oldPassword", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("oldPassword")
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserPutDTO userPutDTO = (UserPutDTO) o;
        return Objects.equals(this.newPassword, userPutDTO.newPassword) &&
                Objects.equals(this.oldPassword, userPutDTO.oldPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newPassword, oldPassword);
    }

    @Override
    public String toString() {
        String sb = "class UserPutDTO {\n" +
                "    newPassword: " + "*" + "\n" +
                "    oldPassword: " + "*" + "\n" +
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

