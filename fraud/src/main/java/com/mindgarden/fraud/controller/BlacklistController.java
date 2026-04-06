package com.mindgarden.fraud.controller;

import com.mindgarden.fraud.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/blacklist")
@RequiredArgsConstructor
@Slf4j
public class BlacklistController {

    private final BlacklistService blacklistService;

    @PostMapping("/{customerId}")
    public ResponseEntity<Void> addCustomerToBlacklist(@PathVariable UUID customerId, @RequestParam String reason) {

        blacklistService.addToBlacklist(customerId, reason);
        return ResponseEntity.noContent()
                             .build();
    }


    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> removeCustomerFromBlacklist(@PathVariable UUID customerId) {

        blacklistService.removeFromBlacklist(customerId);
        return ResponseEntity.noContent()
                             .build();
    }

}
