package com.mindgarden.fraud.config;


import com.mindgarden.fraud.entity.FraudCheckHistory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "http://user-service-url")
public interface FraudClient {
    @GetMapping("/users/{id}")
    FraudCheckHistory getById(@PathVariable UUID id);
}
