package com.allobank.test.service;

import com.allobank.test.dto.settlement.SettlementResponse;

import java.util.List;

public interface SettlementService {
    SettlementResponse createSettlement(Long billingId);
    List<SettlementResponse> historySettlementPerBill(Long billingId);
}
