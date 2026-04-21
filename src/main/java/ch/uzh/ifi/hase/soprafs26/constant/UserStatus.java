package ch.uzh.ifi.hase.soprafs26.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;

/**
 * Gets or Sets UserStatus
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public enum UserStatus {

    ONLINE("ONLINE"),

    OFFLINE("OFFLINE");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static UserStatus fromValue(String value) {
        for (UserStatus b : UserStatus.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

