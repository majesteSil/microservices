package com.mindgarden.fraud.dto;


import com.mindgarden.shared.entity.FraudStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record FraudCheckHistoryResponse(UUID id, UUID customerId, FraudStatus fraudStatus, LocalDateTime createdAt) {
}
