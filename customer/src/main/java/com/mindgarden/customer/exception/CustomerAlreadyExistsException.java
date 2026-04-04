package com.mindgarden.customer.exception;

public class CustomerAlreadyExistsException extends RuntimeException {
    public CustomerAlreadyExistsException(final String email) {
        super(" E-Mail '%s' already used ".formatted(email));
    }
}
