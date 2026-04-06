package com.mindgarden.fraud.controller;

import com.mindgarden.fraud.dto.FraudCheckHistoryResponse;
import com.mindgarden.fraud.service.FraudCheckService;
import com.mindgarden.shared.dto.FraudCheckRequest;
import com.mindgarden.shared.dto.FraudCheckResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("api/v1/fraud-check")
@RequiredArgsConstructor
public class FraudController {

    private final FraudCheckService fraudCheckService;

    @PostMapping
    public ResponseEntity<FraudCheckResponse> checkFraud(@Valid @RequestBody FraudCheckRequest request) {

        return ResponseEntity.ok(fraudCheckService.checkFraud(request.customerId()));
    }

    @GetMapping("/{customerId}/history")
    public ResponseEntity<List<FraudCheckHistoryResponse>> getHistory(@PathVariable UUID customerId) {

        return ResponseEntity.ok(fraudCheckService.getHistoryByCustomerId(customerId));
    }
}
