package com.mindgarden.customer.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CustomerStatus {
    ACTIVE, INACTIVE, BLOCKED;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static CustomerStatus fromValue(String value) {
        for (CustomerStatus status : values()) {
            if (status.name()
                      .equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CustomerStatus: " + value);
    }
}
