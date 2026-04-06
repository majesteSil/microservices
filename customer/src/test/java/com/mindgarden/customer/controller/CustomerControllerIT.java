package com.mindgarden.customer.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.mindgarden.customer.dto.AddressRequest;
import com.mindgarden.customer.dto.CreateCustomerRequest;
import com.mindgarden.customer.dto.CustomerResponse;
import com.mindgarden.customer.dto.UpdateCustomerRequest;
import com.mindgarden.customer.entity.CustomerStatus;
import com.mindgarden.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

class CustomerControllerIT extends com.mindgarden.customer.controller.AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    // Sauberer Zustand vor jedem Test
    @BeforeEach
    void cleanUp() {
        customerRepository.deleteAll();
    }

    // -------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------

    private CreateCustomerRequest buildCreateRequest(String email) {
        return new CreateCustomerRequest("Max", "Mustermann", email, "+49 151 12345678", null);
    }

    private CreateCustomerRequest buildCreateRequestWithAddress(String email) {
        AddressRequest address = new AddressRequest("Musterstraße", "1", "10115", "Berlin", "Deutschland");
        return new CreateCustomerRequest("Max", "Mustermann", email, "+49 151 12345678", address);
    }

    private ResponseEntity<CustomerResponse> createCustomer(String email) {
        return restTemplate.postForEntity("/api/v1/customers", buildCreateRequest(email), CustomerResponse.class);
    }

    // -------------------------------------------------------
    // POST /api/v1/customers
    // -------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/customers")
    class CreateCustomer {

        @Test
        @DisplayName("201 + Customer in DB gespeichert")
        void success() {
            ResponseEntity<CustomerResponse> response = createCustomer("max@example.de");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()
                               .email()).isEqualTo("max@example.de");
            assertThat(response.getBody()
                               .status()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customerRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("201 + Adresse optional mitgeben")
        void withAddress() {
            ResponseEntity<CustomerResponse> response = restTemplate.postForEntity("/api/v1/customers",
                                                                                   buildCreateRequestWithAddress(
                                                                                           "adresse@example.de"),
                                                                                   CustomerResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()
                               .address()).isNotNull();
            assertThat(response.getBody()
                               .address()
                               .city()).isEqualTo("Berlin");
        }

        @Test
        @DisplayName("409 bei doppelter E-Mail")
        void duplicateEmail() {
            createCustomer("dup@example.de");
            ResponseEntity<String> second = restTemplate.postForEntity("/api/v1/customers",
                                                                       buildCreateRequest("dup@example.de"),
                                                                       String.class);

            assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("400 bei leerem Firstname")
        void validationError() {
            CreateCustomerRequest invalid = new CreateCustomerRequest("", "M", "valid@example.de", "+49 151 0", null);
            ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/customers", invalid, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }


    @Nested
    @DisplayName("GET /api/v1/customers/{id}")
    class GetCustomer {

        @Test
        @DisplayName("200 + korrekter Customer")
        void found() {
            CustomerResponse created = createCustomer("get@example.de").getBody();

            ResponseEntity<CustomerResponse> response = restTemplate.getForEntity("/api/v1/customers/" + created.id(),
                                                                                  CustomerResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()
                               .id()).isEqualTo(created.id());
        }

        @Test
        @DisplayName("404 bei unbekannter ID")
        void notFound() {
            ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/customers/" + UUID.randomUUID(),
                                                                        String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/customers/{id}")
    class UpdateCustomer {

        @Test
        @DisplayName("200 + Felder aktualisiert")
        void success() {
            CustomerResponse created = createCustomer("update@example.de").getBody();

            UpdateCustomerRequest updateRequest = new UpdateCustomerRequest("Maximilian",
                                                                            "Mustermann",
                                                                            "update@example.de",
                                                                            "+49 151 99999999",
                                                                            null);

            ResponseEntity<CustomerResponse> response = restTemplate.exchange("/api/v1/customers/" + created.id(),
                                                                              HttpMethod.PUT,
                                                                              new HttpEntity<>(updateRequest),
                                                                              CustomerResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()
                               .firstname()).isEqualTo("Maximilian");
            assertThat(response.getBody()
                               .phone()).isEqualTo("+49 151 99999999");
        }
    }

    // -------------------------------------------------------
    // Status-Übergänge
    // -------------------------------------------------------

    @Nested
    @DisplayName("Statusübergänge")
    class StatusTransitions {

        @Test
        @DisplayName("POST /{id}/deactivate → 200 + Status INACTIVE")
        void deactivate() {
            CustomerResponse created = createCustomer("deact@example.de").getBody();

            ResponseEntity<CustomerResponse> response = restTemplate.postForEntity("/api/v1/customers/" + created.id() + "/deactivate",
                                                                                   null,
                                                                                   CustomerResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()
                               .status()).isEqualTo(CustomerStatus.INACTIVE);
        }

        @Test
        @DisplayName("POST /{id}/activate → 200 + Status ACTIVE")
        void activate() {
            CustomerResponse created = createCustomer("act@example.de").getBody();

            // Erst deaktivieren
            restTemplate.postForEntity("/api/v1/customers/" + created.id() + "/deactivate", null, Void.class);

            // Dann aktivieren
            ResponseEntity<CustomerResponse> response = restTemplate.postForEntity("/api/v1/customers/" + created.id() + "/activate",
                                                                                   null,
                                                                                   CustomerResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()
                               .status()).isEqualTo(CustomerStatus.ACTIVE);
        }

        @Test
        @DisplayName("PATCH /{id}/block → 200 + Status BLOCKED")
        void block() {
            CustomerResponse created = createCustomer("block@example.de").getBody();

            ResponseEntity<CustomerResponse> response = restTemplate.exchange("/api/v1/customers/" + created.id() + "/block",
                                                                              HttpMethod.PATCH,
                                                                              null,
                                                                              CustomerResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()
                               .status()).isEqualTo(CustomerStatus.BLOCKED);
        }

        @Test
        @DisplayName("409 bei doppeltem block")
        void blockAlreadyBlocked() {
            CustomerResponse created = createCustomer("blocked2@example.de").getBody();

            restTemplate.exchange("/api/v1/customers/" + created.id() + "/block", HttpMethod.PATCH, null, Void.class);

            ResponseEntity<String> second = restTemplate.exchange("/api/v1/customers/" + created.id() + "/block",
                                                                  HttpMethod.PATCH,
                                                                  null,
                                                                  String.class);

            assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    // -------------------------------------------------------
    // GET /api/v1/customers/search?email=
    // -------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/customers/search")
    class SearchByEmail {

        @Test
        @DisplayName("200 + Customer gefunden")
        void found() {
            createCustomer("search@example.de");

            ResponseEntity<CustomerResponse> response = restTemplate.getForEntity(
                    "/api/v1/customers/search?email=search@example.de",
                    CustomerResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()
                               .email()).isEqualTo("search@example.de");
        }

        @Test
        @DisplayName("404 bei unbekannter E-Mail")
        void notFound() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "/api/v1/customers/search?email=ghost@example.de",
                    String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
