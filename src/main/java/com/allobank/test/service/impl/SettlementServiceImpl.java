package com.allobank.test.service.impl;

import com.allobank.test.dto.settlement.SettlementResponse;
import com.allobank.test.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementServiceImpl implements SettlementService {
    @Override
    public SettlementResponse createSettlement(Long billingId) {
        return null;
    }

    @Override
    public SettlementResponse latestSettlement(Long billingId) {
        return null;
    }

    @Override
    public List<SettlementResponse> historySettlementPerBill(Long billingId) {
        return List.of();
    }
}
