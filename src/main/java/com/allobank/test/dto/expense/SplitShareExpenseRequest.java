package com.allobank.test.dto.expense;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitShareExpenseRequest {
    @NotNull(message = "participantId is required in each split entry")
    private Long participantId;
    private BigDecimal share;
}
