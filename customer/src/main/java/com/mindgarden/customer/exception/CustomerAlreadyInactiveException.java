package com.mindgarden.customer.exception;

import java.util.UUID;

public class CustomerAlreadyInactiveException extends RuntimeException {
    public CustomerAlreadyInactiveException(UUID id) {
        super("Customer with id '%s' is already inactive".formatted(id));
    }
}
