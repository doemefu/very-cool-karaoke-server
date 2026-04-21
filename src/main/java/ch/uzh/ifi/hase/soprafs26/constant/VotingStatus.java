package ch.uzh.ifi.hase.soprafs26.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;

/**
 * Gets or Sets VotingStatus
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-23T14:36:59.836313+01:00[Europe/Zurich]", comments = "Generator version: 7.7.0")
public enum VotingStatus {

    OPEN("OPEN"),

    CLOSED("CLOSED");

    private final String value;

    VotingStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static VotingStatus fromValue(String value) {
        for (VotingStatus b : VotingStatus.values()) {
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

