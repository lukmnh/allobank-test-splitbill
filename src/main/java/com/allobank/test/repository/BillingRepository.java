package com.allobank.test.repository;

import com.allobank.test.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {
    @Query("SELECT b FROM Billing b LEFT JOIN FETCH b.participants WHERE b.id = :id")
    Optional<Billing> findByIdWithParticipants(@Param("id") Long id);
}
