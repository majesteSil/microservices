package com.mindgarden.customer.repository;

import com.mindgarden.customer.entity.Customer;
import com.mindgarden.customer.entity.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Customer> findByStatus(CustomerStatus status);
}
