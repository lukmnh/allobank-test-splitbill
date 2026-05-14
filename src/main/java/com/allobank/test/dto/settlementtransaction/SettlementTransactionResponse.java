package com.allobank.test.dto.settlementtransaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementTransactionResponse {
    private Long fromParticipantId;
    private String fromParticipantName;
    private Long toParticipantId;
    private String toParticipantName;
    private BigDecimal amount;
}
