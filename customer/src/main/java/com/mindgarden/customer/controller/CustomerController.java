package com.mindgarden.customer.controller;

import com.mindgarden.customer.dto.CreateCustomerRequest;
import com.mindgarden.customer.dto.CustomerResponse;
import com.mindgarden.customer.dto.UpdateCustomerRequest;
import com.mindgarden.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller – ausschließlich HTTP-Verantwortung: - Request entgegennehmen - @Valid auslösen (Validierung des DTOs) -
 * Service aufrufen - HTTP-Status setzen und Response zurückgeben
 *
 * Keine Businesslogik hier. Kein direkter Datenbankzugriff. Wenn der Controller mehr als 5-6 Methoden hat → aufteilen.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;


    /**
     * POST /api/v1/customers
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(response);
    }

    /**
     * GET /api/v1/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    /**
     * GET /api/v1/customers
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    /**
     * PUT /api/v1/customers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable UUID id,
                                                           @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }


    /**
     * POST /api/v1/customers/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<CustomerResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.deactivate(id));
    }

    /**
     * POST /api/v1/customers/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<CustomerResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.activate(id));
    }


    /**
     * GET /api/v1/customers/search
     */
    @GetMapping("/search")
    public ResponseEntity<CustomerResponse> getByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(customerService.getByEmail(email));
    }

    /**
     * PATCH /api/v1/customers/{id}/block Kunden sperren. PATCH, weil wir nur den Status ändern, nicht den gesamten
     * Kunden ersetzen (das wäre PUT).
     */
    @PatchMapping("/{id}/block")
    public ResponseEntity<CustomerResponse> blockCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.blockCustomer(id));
    }
}
