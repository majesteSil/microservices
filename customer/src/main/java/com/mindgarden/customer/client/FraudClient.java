package com.mindgarden.customer.client;

import com.mindgarden.shared.dto.FraudCheckRequest;
import com.mindgarden.shared.dto.FraudCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "fraud-service")
public interface FraudClient {

@PostMapping("/api/v1/fraud-check")
FraudCheckResponse checkFraud(@RequestBody FraudCheckRequest resquest);

}
