package com.allobank.test.service;

import com.allobank.test.dto.expense.ExpenseCreateRequest;
import com.allobank.test.dto.expense.ExpenseResponse;

import java.util.List;

public interface ExpenseService {
    ExpenseResponse createExpense(Long billingId, ExpenseCreateRequest request);
    List<ExpenseResponse> list(Long billingId);
    ExpenseResponse detail(Long billingId, Long expenseId);
}
