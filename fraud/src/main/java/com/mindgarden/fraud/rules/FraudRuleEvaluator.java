package com.mindgarden.fraud.rules;

import com.mindgarden.fraud.repository.BlacklistedCustomerRepository;
import com.mindgarden.fraud.repository.FraudCheckHistoryRepository;
import com.mindgarden.shared.entity.FraudStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudRuleEvaluator {

    private static final int MAX_CHECKS_PER_DAY = 3;
    private final FraudCheckHistoryRepository fraudCheckHistoryRepository;
    private final BlacklistedCustomerRepository blacklistedCustomerRepository;


    public FraudStatus evaluate(UUID customerId) {

        if (isBlacklisted(customerId)) {
            log.warn("Fraud rule violated — customer is blacklisted: customerId={}", customerId);
            return FraudStatus.REJECTED;
        }

        if (isTooManyChecks(customerId)) {

            log.warn("Fraud rule violated -too many checks within the last 24 hrs: customerId={}", customerId);
            return FraudStatus.REJECTED;
        }
        return FraudStatus.APPROVED;
    }

    private boolean isBlacklisted(UUID customerId) {
        return blacklistedCustomerRepository.existsByCustomerId(customerId);
    }

    private boolean isTooManyChecks(final UUID customerId) {
        LocalDateTime since = LocalDateTime.now()
                                           .minusHours(24);
        long count = fraudCheckHistoryRepository.countByCustomerIdAndCreatedAtAfter(customerId, since);

        log.debug("Checks in last 24h for customerId={}: {}", customerId, count);
        return count >= MAX_CHECKS_PER_DAY;
    }
}
