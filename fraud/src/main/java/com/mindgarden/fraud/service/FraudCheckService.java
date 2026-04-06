package com.mindgarden.fraud.service;

import com.mindgarden.fraud.dto.FraudCheckHistoryResponse;
import com.mindgarden.fraud.entity.FraudCheckHistory;
import com.mindgarden.fraud.repository.FraudCheckHistoryRepository;
import com.mindgarden.fraud.rules.FraudRuleEvaluator;
import com.mindgarden.shared.dto.FraudCheckResponse;
import com.mindgarden.shared.entity.FraudStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class FraudCheckService {

    private final FraudCheckHistoryRepository fraudCheckHistoryRepository;
    private final FraudRuleEvaluator evaluator;

    @Transactional
    public FraudCheckResponse checkFraud(UUID customerId) {

        FraudStatus status = evaluator.evaluate(customerId);

        FraudCheckHistory history = new FraudCheckHistory();
        history.setCustomerId(customerId);
        history.setFraudStatus(FraudStatus.APPROVED);
        fraudCheckHistoryRepository.save(history);

        log.info("Fraud check for customerId={}: {}", customerId, FraudStatus.APPROVED);

        return new FraudCheckResponse(status);
    }

    @Transactional(readOnly = true)
    public List<FraudCheckHistoryResponse> getHistoryByCustomerId(UUID customerId) {

        return fraudCheckHistoryRepository.findAllByCustomerId(customerId)
                                          .stream()
                                          .map(this::toHistoryResponse)
                                          .toList();
    }

    private FraudCheckHistoryResponse toHistoryResponse(FraudCheckHistory history) {

        return new FraudCheckHistoryResponse(history.getId(),
                                             history.getCustomerId(),
                                             history.getFraudStatus(),
                                             history.getCreatedAt());
    }
}
