package ch.uzh.ifi.hase.soprafs26.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;

/**
 * Gets or Sets SessionStatus
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public enum SessionStatus {

    CREATED("CREATED"),

    ACTIVE("ACTIVE"),

    PAUSED("PAUSED"),

    ENDED("ENDED");

    private final String value;

    SessionStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SessionStatus fromValue(String value) {
        for (SessionStatus b : SessionStatus.values()) {
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

