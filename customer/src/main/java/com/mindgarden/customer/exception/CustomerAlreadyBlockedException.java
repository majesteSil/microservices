package com.mindgarden.customer.exception;

import java.util.UUID;

public class CustomerAlreadyBlockedException extends RuntimeException {
    public CustomerAlreadyBlockedException(UUID id) {
        super("Customer with id '%s' is already blocked".formatted(id));
    }
}
