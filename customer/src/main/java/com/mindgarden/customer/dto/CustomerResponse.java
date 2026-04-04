package com.mindgarden.customer.dto;

import com.mindgarden.customer.entity.CustomerStatus;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(UUID id, String firstname, String lastname, String email, String phone,
                               AddressResponse address, CustomerStatus status, Instant createdAt) {
}
