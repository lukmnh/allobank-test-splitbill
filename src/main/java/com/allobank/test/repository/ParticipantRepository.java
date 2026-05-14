package com.allobank.test.repository;

import com.allobank.test.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByIdAndBillingId(Long id, Long billingId);
    List<Participant> findByBillingId(Long billingId);
}
