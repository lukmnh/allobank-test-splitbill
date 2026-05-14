package com.allobank.test.repository;

import com.allobank.test.entity.ExpenseShare;
import com.allobank.test.entity.projection.ExpenseShareProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
    @Query("""
    SELECT s.participant.name AS participantName,
           s.amount           AS amount,
           s.percentageShare  AS percentageShare
    FROM ExpenseShare s
    WHERE s.expense.id = :expenseId
    """)
    List<ExpenseShareProjection> findShareProjectionsByExpenseId(@Param("expenseId") Long expenseId);
}
