package com.allobank.test.controller;

import com.allobank.test.dto.expense.ExpenseCreateRequest;
import com.allobank.test.dto.expense.ExpenseResponse;
import com.allobank.test.dto.expense.SplitShareExpenseRequest;
import com.allobank.test.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing/{billingId}/expense")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@PathVariable Long billingId,
                                                      @Valid @RequestBody ExpenseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(billingId, request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(
            @PathVariable Long billingId) {
        return ResponseEntity.ok(expenseService.list(billingId));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> detail(
            @PathVariable Long billingId,
            @PathVariable Long expenseId) {
        return ResponseEntity.ok(expenseService.detail(billingId, expenseId));
    }

}
