package com.mindgarden.shared.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FraudStatus {

    APPROVED, REJECTED;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static FraudStatus fromValue(String value) {
        for (FraudStatus status : values()) {
            if (status.name()
                      .equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown FraudStatus: " + value);
    }
}
