package com.mindgarden.fraud.service;


import com.mindgarden.fraud.entity.BlacklistedCustomer;
import com.mindgarden.fraud.repository.BlacklistedCustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistService {

    private final BlacklistedCustomerRepository blacklistedCustomerRepository;

    @Transactional
    public void addToBlacklist(UUID customerId, String reason) {
        if (blacklistedCustomerRepository.existsByCustomerId(customerId)) {
            log.warn("Customer already blacklisted: {}", customerId);
            return;
        }
        BlacklistedCustomer blacklisted = new BlacklistedCustomer();
        blacklisted.setCustomerId(customerId);
        blacklisted.setReason(reason);

        blacklistedCustomerRepository.save(blacklisted);
        log.info("Customer added to blacklist: customerId={}, reason={}", customerId, reason);
    }

    public void removeFromBlacklist(UUID customerId) {
        blacklistedCustomerRepository.findByCustomerId(customerId)
                                     .ifPresentOrElse(blacklistedCustomer -> {
                                         blacklistedCustomerRepository.delete(blacklistedCustomer);
                                         log.info("Customer removed from blacklist: customerId={}", customerId);
                                     }, () -> log.warn("Customer not found in blacklist: customerId={}", customerId));
    }
}
