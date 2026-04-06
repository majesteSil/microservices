package com.mindgarden.shared.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FraudCheckRequest(@NotNull UUID customerId) {
}
