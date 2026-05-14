package com.allobank.test.dto.expense;

import com.allobank.test.constant.ExpenseType;
import com.allobank.test.constant.SplitStrategyEnum;
import com.allobank.test.entity.Expense;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
public class ExpenseCreateRequest {
    @NotBlank(message = "Description is required")
    @Size(max = 255)
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @NotNull(message = "Category is required")
    private ExpenseType type;

    @NotNull(message = "paid_by_id is required")
    private Long paidById;

    @NotNull(message = "split_strategy is required")
    private SplitStrategyEnum splitStrategy;

    @NotEmpty(message = "splits must not be empty")
    @Valid
    private List<SplitShareExpenseRequest> splits;
}
