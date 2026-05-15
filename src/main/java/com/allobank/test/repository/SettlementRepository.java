package com.allobank.test.repository;

import com.allobank.test.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    @Query("""
    SELECT s FROM Settlement s
    LEFT JOIN FETCH s.transactions t
    LEFT JOIN FETCH t.fromParticipant
    LEFT JOIN FETCH t.toParticipant
    WHERE s.billing.id = :billingId
    ORDER BY s.createdAt DESC
    """)
    List<Settlement> findByBillingIdWithTransactions(@Param("billingId") Long billingId);
}
