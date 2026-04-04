package com.mindgarden.customer.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(UUID id) {
        super("Customer with id '%s' not found".formatted(id));
    }
    public CustomerNotFoundException(String email) {
        super("Customer with id '%s' not found".formatted(email));
    }
}
