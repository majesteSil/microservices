package com.mindgarden.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mindgarden.customer.dto.CreateCustomerRequest;
import com.mindgarden.customer.dto.CustomerResponse;
import com.mindgarden.customer.dto.UpdateCustomerRequest;
import com.mindgarden.customer.entity.Customer;
import com.mindgarden.customer.entity.CustomerStatus;
import com.mindgarden.customer.exception.CustomerAlreadyActiveException;
import com.mindgarden.customer.exception.CustomerAlreadyBlockedException;
import com.mindgarden.customer.exception.CustomerAlreadyExistsException;
import com.mindgarden.customer.exception.CustomerAlreadyInactiveException;
import com.mindgarden.customer.exception.CustomerNotFoundException;
import com.mindgarden.customer.exception.EmailAlreadyExistsException;
import com.mindgarden.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)  // ① kein Spring-Context, nur Mockito
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    // -------------------------------------------------------
    // Test-Fixtures — einmal definieren, überall verwenden
    // -------------------------------------------------------

    private UUID customerId;
    private Customer activeCustomer;
    private CreateCustomerRequest createRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        activeCustomer = new Customer();
        activeCustomer.setId(customerId);
        activeCustomer.setFirstname("Max");
        activeCustomer.setLastname("Mustermann");
        activeCustomer.setEmail("max@example.de");
        activeCustomer.setPhone("+49 151 12345678");
        activeCustomer.setStatus(CustomerStatus.ACTIVE);
        activeCustomer.setCreatedAt(Instant.now());
        activeCustomer.setUpdatedAt(Instant.now());

        createRequest = new CreateCustomerRequest("Max", "Mustermann", "max@example.de", "+49 151 12345678", null);
    }

    // -------------------------------------------------------
    // createCustomer
    // -------------------------------------------------------

    @Nested
    @DisplayName("createCustomer()")
    class CreateCustomer {

        @Test
        @DisplayName("Erfolg → Customer wird gespeichert und zurückgegeben")
        void success() {
            when(customerRepository.existsByEmail("max@example.de")).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenReturn(activeCustomer);

            CustomerResponse response = customerService.createCustomer(createRequest);

            assertThat(response.email()).isEqualTo("max@example.de");
            assertThat(response.status()).isEqualTo(CustomerStatus.ACTIVE);
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Duplikat-E-Mail → EmailAlreadyExistsException")
        void duplicateEmail() {
            when(customerRepository.existsByEmail("max@example.de")).thenReturn(true);

            assertThatThrownBy(() -> customerService.createCustomer(createRequest)).isInstanceOf(
                    EmailAlreadyExistsException.class);

            verify(customerRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------
    // getCustomer
    // -------------------------------------------------------

    @Nested
    @DisplayName("getCustomer()")
    class GetCustomer {

        @Test
        @DisplayName("Gefunden → Response zurückgeben")
        void found() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));

            CustomerResponse response = customerService.getCustomer(customerId);

            assertThat(response.id()).isEqualTo(customerId);
        }

        @Test
        @DisplayName("Nicht gefunden → CustomerNotFoundException")
        void notFound() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getCustomer(customerId)).isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // -------------------------------------------------------
    // getAllCustomers
    // -------------------------------------------------------

    @Nested
    @DisplayName("getAllCustomers()")
    class GetAllCustomers {

        @Test
        @DisplayName("Zwei Kunden in DB → Liste mit zwei Einträgen")
        void returnsList() {
            Customer second = new Customer();
            second.setId(UUID.randomUUID());
            second.setFirstname("Erika");
            second.setLastname("Musterfrau");
            second.setEmail("erika@example.de");
            second.setPhone("+49 151 99999999");
            second.setStatus(CustomerStatus.ACTIVE);
            second.setCreatedAt(Instant.now());
            second.setUpdatedAt(Instant.now());

            when(customerRepository.findAll()).thenReturn(List.of(activeCustomer, second));

            List<CustomerResponse> result = customerService.getAllCustomers();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Keine Kunden → leere Liste, keine Exception")
        void emptyList() {
            when(customerRepository.findAll()).thenReturn(List.of());

            assertThat(customerService.getAllCustomers()).isEmpty();
        }
    }

    // -------------------------------------------------------
    // updateCustomer
    // -------------------------------------------------------

    @Nested
    @DisplayName("updateCustomer()")
    class UpdateCustomer {

        private UpdateCustomerRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new UpdateCustomerRequest("Maximilian",
                                                      "Mustermann",
                                                      "max@example.de",
                                                      "+49 151 00000000",
                                                      null);
        }

        @Test
        @DisplayName("Erfolg → Felder werden aktualisiert")
        void success() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));

            CustomerResponse response = customerService.updateCustomer(customerId, updateRequest);

            assertThat(response.firstname()).isEqualTo("Maximilian");
            assertThat(response.phone()).isEqualTo("+49 151 00000000");
        }

        @Test
        @DisplayName("Neue E-Mail bereits vergeben → CustomerAlreadyExistsException")
        void emailTaken() {
            UpdateCustomerRequest withNewEmail = new UpdateCustomerRequest("Max",
                                                                           "M",
                                                                           "andere@example.de",
                                                                           "+49 151 0",
                                                                           null);
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));
            when(customerRepository.existsByEmail("andere@example.de")).thenReturn(true);

            assertThatThrownBy(() -> customerService.updateCustomer(customerId, withNewEmail)).isInstanceOf(
                    CustomerAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Gleiche E-Mail behalten → kein Duplikat-Check nötig")
        void sameEmailNoCheck() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));

            // gleiche E-Mail → existsByEmail darf NICHT aufgerufen werden
            customerService.updateCustomer(customerId, updateRequest);

            verify(customerRepository, never()).existsByEmail(any());
        }
    }

    // -------------------------------------------------------
    // Status-Übergänge
    // -------------------------------------------------------

    @Nested
    @DisplayName("Statusübergänge")
    class StatusTransitions {

        @Test
        @DisplayName("deactivate: ACTIVE → INACTIVE")
        void deactivateSuccess() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));
            when(customerRepository.save(activeCustomer)).thenReturn(activeCustomer);

            customerService.deactivate(customerId);

            assertThat(activeCustomer.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
        }

        @Test
        @DisplayName("deactivate: bereits INACTIVE → CustomerAlreadyInactiveException")
        void deactivateAlreadyInactive() {
            activeCustomer.setStatus(CustomerStatus.INACTIVE);
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));

            assertThatThrownBy(() -> customerService.deactivate(customerId)).isInstanceOf(
                    CustomerAlreadyInactiveException.class);
        }

        @Test
        @DisplayName("activate: INACTIVE → ACTIVE")
        void activateSuccess() {
            activeCustomer.setStatus(CustomerStatus.INACTIVE);
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));
            when(customerRepository.save(activeCustomer)).thenReturn(activeCustomer);

            customerService.activate(customerId);

            assertThat(activeCustomer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        }

        @Test
        @DisplayName("activate: bereits ACTIVE → CustomerAlreadyActiveException")
        void activateAlreadyActive() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));

            assertThatThrownBy(() -> customerService.activate(customerId)).isInstanceOf(CustomerAlreadyActiveException.class);
        }

        @Test
        @DisplayName("blockCustomer: ACTIVE → BLOCKED")
        void blockSuccess() {
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));
            when(customerRepository.save(activeCustomer)).thenReturn(activeCustomer);

            customerService.blockCustomer(customerId);

            assertThat(activeCustomer.getStatus()).isEqualTo(CustomerStatus.BLOCKED);
        }

        @Test
        @DisplayName("blockCustomer: bereits BLOCKED → CustomerAlreadyBlockedException")
        void blockAlreadyBlocked() {
            activeCustomer.setStatus(CustomerStatus.BLOCKED);
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));

            assertThatThrownBy(() -> customerService.blockCustomer(customerId)).isInstanceOf(
                    CustomerAlreadyBlockedException.class);
        }
    }

    // -------------------------------------------------------
    // getByEmail
    // -------------------------------------------------------

    @Nested
    @DisplayName("getByEmail()")
    class GetByEmail {

        @Test
        @DisplayName("E-Mail existiert → Customer zurückgeben")
        void found() {
            when(customerRepository.findByEmail("max@example.de")).thenReturn(Optional.of(activeCustomer));

            CustomerResponse response = customerService.getByEmail("max@example.de");

            assertThat(response.email()).isEqualTo("max@example.de");
        }

        @Test
        @DisplayName("E-Mail nicht gefunden → CustomerNotFoundException")
        void notFound() {
            when(customerRepository.findByEmail("ghost@example.de")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getByEmail("ghost@example.de")).isInstanceOf(
                    CustomerNotFoundException.class);
        }
    }
}
