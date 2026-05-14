package com.allobank.test.dto.expense;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SplitShareExpenseResponse {
    private String participantId;
    private String participantName;
    private BigDecimal amount;
    private BigDecimal percentageShare;
}
