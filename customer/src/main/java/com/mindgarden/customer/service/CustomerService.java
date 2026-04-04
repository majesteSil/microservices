package com.mindgarden.customer.service;

import com.mindgarden.customer.dto.AddressRequest;
import com.mindgarden.customer.dto.AddressResponse;
import com.mindgarden.customer.dto.CreateCustomerRequest;
import com.mindgarden.customer.dto.CustomerResponse;
import com.mindgarden.customer.dto.UpdateCustomerRequest;
import com.mindgarden.customer.entity.Address;
import com.mindgarden.customer.entity.Customer;
import com.mindgarden.customer.entity.CustomerStatus;
import com.mindgarden.customer.exception.CustomerAlreadyActiveException;
import com.mindgarden.customer.exception.CustomerAlreadyBlockedException;
import com.mindgarden.customer.exception.CustomerAlreadyExistsException;
import com.mindgarden.customer.exception.CustomerAlreadyInactiveException;
import com.mindgarden.customer.exception.CustomerNotFoundException;
import com.mindgarden.customer.exception.EmailAlreadyExistsException;
import com.mindgarden.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        // Businessregel: E-Mail muss eindeutig sein
        if (customerRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Customer customer = toEntity(request);
        Customer saved = customerRepository.save(customer);

        log.info("Customer created: id={}, email={}", saved.getId(), saved.getEmail());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                                              .orElseThrow(() -> new CustomerNotFoundException(id));
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                                 .stream()
                                 .map(this::toResponse)
                                 .toList();
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                                              .orElseThrow(() -> new CustomerNotFoundException(id));

        // E-Mail-Änderung: nur prüfen, wenn die E-Mail wirklich neu ist
        if (!customer.getEmail()
                     .equals(request.email()) && customerRepository.existsByEmail(request.email())) {
            throw new CustomerAlreadyExistsException(request.email());
        }

        customer.setFirstname(request.firstname());
        customer.setLastname(request.lastname());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());

        if (request.address() != null) {
            customer.setAddress(toAddressEntity(request.address()));
        }

        // Kein explizites save() nötig:
        // @Transactional + Hibernate Dirty-Checking erkennt Änderungen
        // und schreibt automatisch ein UPDATE am Transaktionsende.
        log.info("Customer updated: id={}", id);
        return toResponse(customer);
    }

    @Transactional
    public CustomerResponse deactivate(final UUID id) {

        Customer customer = customerRepository.findById(id)
                                              .orElseThrow(() -> new CustomerNotFoundException(id));

        if (customer.getStatus()
                    .equals(CustomerStatus.INACTIVE)) {
            throw new CustomerAlreadyInactiveException(id);
        }
        customer.setStatus(CustomerStatus.INACTIVE);
        return toResponse(customerRepository.save(customer));

    }

    @Transactional
    public CustomerResponse activate(final UUID id) {
        Customer customer = customerRepository.findById(id)
                                              .orElseThrow(() -> new CustomerNotFoundException(id));
        if (customer.getStatus()
                    .equals(CustomerStatus.ACTIVE)) {
            throw new CustomerAlreadyActiveException(id);
        }

        //TODO INACTIVE und BLOCKED → beide dürfen aktiviert werden
        customer.setStatus(CustomerStatus.ACTIVE);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse blockCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                                              .orElseThrow(() -> new CustomerNotFoundException(id));
        if (customer.getStatus()
                    .equals(CustomerStatus.BLOCKED)) {
            throw new CustomerAlreadyBlockedException(id);
        }
        customer.setStatus(CustomerStatus.BLOCKED);
        log.info("Customer blocked: id={}", id);
        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse getByEmail(final String email) {

        return customerRepository.findByEmail(email)
                                 .map(this::toResponse)
                                 .orElseThrow(() -> new CustomerNotFoundException(email));
    }

    private Customer toEntity(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstname(request.firstname());
        customer.setLastname(request.lastname());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());

        if (request.address() != null) {
            customer.setAddress(toAddressEntity(request.address()));
        }

        return customer;
    }

    private Address toAddressEntity(AddressRequest dto) {
        return new Address(dto.street(), dto.houseNumber(), dto.zipCode(), dto.city(), dto.country());
    }

    private CustomerResponse toResponse(Customer customer) {
        AddressResponse addressResponse = null;
        if (customer.getAddress() != null) {
            addressResponse = new AddressResponse(customer.getAddress()
                                                          .getStreet(),
                                                  customer.getAddress()
                                                          .getHouseNumber(),
                                                  customer.getAddress()
                                                          .getZipCode(),
                                                  customer.getAddress()
                                                          .getCity(),
                                                  customer.getAddress()
                                                          .getCountry());
        }

        return new CustomerResponse(customer.getId(),
                                    customer.getFirstname(),
                                    customer.getLastname(),
                                    customer.getEmail(),
                                    customer.getPhone(),
                                    addressResponse,
                                    customer.getStatus(),
                                    customer.getCreatedAt());
    }

}
