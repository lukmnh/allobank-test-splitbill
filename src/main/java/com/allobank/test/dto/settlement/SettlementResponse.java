package com.allobank.test.dto.settlement;

import com.allobank.test.dto.expense.ExpenseResponse;
import com.allobank.test.dto.payment.PaymentResponse;
import com.allobank.test.dto.settlementtransaction.SettlementTransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {
    private String settlementId;
    private String groupId;
    private String groupName;
    private BigDecimal totalExpenses;
    private String serviceChargePct;
    private BigDecimal serviceChargeAmount;
    private BigDecimal grandTotal;
    private Map<String, BigDecimal> balances;
    private List<SettlementTransactionResponse> transactions;
    private Map<String, BigDecimal> categoryBreakdown;
    private List<ExpenseResponse> expenses;
    private List<PaymentResponse> payments;
    private String settledAt;
}
