package com.allobank.test.repository;

import com.allobank.test.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("""
        SELECT p FROM Payment p
        JOIN FETCH p.payer
        JOIN FETCH p.payee
        WHERE p.billing.id = :billingId
        ORDER BY p.paidAt ASC
        """)
    List<Payment> findByBillingIdWithParticipants(@Param("billingId") Long billingId);
}
