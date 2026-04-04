package com.mindgarden.customer.exception;

import java.util.UUID;

public class CustomerAlreadyActiveException extends RuntimeException {
    public CustomerAlreadyActiveException(UUID id) {
        super("Customer with id '%s' is already active".formatted(id));
    }
}
