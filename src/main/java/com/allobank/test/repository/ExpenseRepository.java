package com.allobank.test.repository;

import com.allobank.test.entity.Expense;
import com.allobank.test.entity.ExpenseShare;
import com.allobank.test.entity.projection.ExpenseDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByBillingId(Long billingId);

    @Query("""
    SELECT e.id          AS id,
           e.description AS description,
           e.amount      AS amount,
           e.expenseType AS expenseType,
           e.splitStrategy AS splitStrategy,
           e.createdAt   AS createdAt,
           b.id          AS billingId,
           b.name        AS billingName,
           p.name        AS paidByName
    FROM Expense e
    JOIN e.billing b
    JOIN e.paidBy p
    WHERE e.id = :expenseId AND b.id = :billingId
    """)
    Optional<ExpenseDetailProjection> findDetailById(
            @Param("expenseId") Long expenseId,
            @Param("billingId") Long billingId);

    @Query("""
        SELECT e 
        FROM Expense e
        JOIN FETCH e.paidBy
        JOIN FETCH e.shares s
        JOIN FETCH s.participant
        WHERE e.billing.id = :billingId
        ORDER BY e.createdAt ASC
    """)
    List<Expense> findByBillingIdWithShares(@Param("billingId") Long billingId);
}
