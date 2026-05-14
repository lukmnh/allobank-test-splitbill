package com.allobank.test.entity.projection;

import com.allobank.test.constant.ExpenseType;
import com.allobank.test.constant.SplitStrategyEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ExpenseDetailProjection {
    Long getId();
    String getDescription();
    BigDecimal getAmount();
    ExpenseType getExpenseType();
    SplitStrategyEnum getSplitStrategy();
    LocalDateTime getCreatedAt();
    Long getBillingId();
    String getBillingName();
    String getPaidByName();
}
