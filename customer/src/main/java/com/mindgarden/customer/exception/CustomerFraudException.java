package com.mindgarden.customer.exception;

import java.util.UUID;

public class CustomerFraudException extends RuntimeException {
    public CustomerFraudException(final UUID id) {
        super("Customer with id '%s' is a Fraudster".formatted(id));
    }
}
