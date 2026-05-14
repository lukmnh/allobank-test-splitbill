package com.allobank.test.dto.expense;

import com.allobank.test.constant.ExpenseType;
import com.allobank.test.constant.SplitStrategyEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenseResponse {
    private String id;
    private String description;
    private BigDecimal amount;
    private ExpenseType type;
    private SplitStrategyEnum splitStrategy;
    private String paidById;
    private String paidByName;
    private List<SplitShareExpenseResponse> shares;
    private String createdAt;
}
