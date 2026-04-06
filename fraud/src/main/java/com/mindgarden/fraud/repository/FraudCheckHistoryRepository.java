package com.mindgarden.fraud.repository;

import com.mindgarden.fraud.entity.FraudCheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface FraudCheckHistoryRepository extends JpaRepository<FraudCheckHistory, UUID> {
    List<FraudCheckHistory> findAllByCustomerId(UUID customerId);

    @Query("""
            SELECT COUNT(f) FROM FraudCheckHistory f
            WHERE f.customerId = :customerId
            AND f.createdAt >= :since
            """)
    long countByCustomerIdAndCreatedAtAfter(
            @Param("customerId") UUID customerId,
            @Param("since") LocalDateTime since
                                           );
}
