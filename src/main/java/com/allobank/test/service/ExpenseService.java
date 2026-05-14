package com.allobank.test.service;

import com.allobank.test.dto.expense.ExpenseCreateRequest;
import com.allobank.test.dto.expense.ExpenseResponse;

public interface ExpenseService {
    ExpenseResponse createExpense(Long billingId, ExpenseCreateRequest request);
}
