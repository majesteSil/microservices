package com.mindgarden.fraud.repository;

import com.mindgarden.fraud.entity.BlacklistedCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistedCustomerRepository extends JpaRepository<BlacklistedCustomer, UUID> {

    boolean existsByCustomerId(UUID customerId);

    Optional<BlacklistedCustomer> findByCustomerId(UUID customerId);

}
