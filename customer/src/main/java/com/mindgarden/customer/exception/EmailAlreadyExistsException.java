package com.mindgarden.customer.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("A Customer with this Mail :'" + email + "' Already Exists");
    }
}
