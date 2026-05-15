package com.allobank.test.controller;

import com.allobank.test.dto.settlement.SettlementResponse;
import com.allobank.test.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/billing/{billingId}/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<SettlementResponse> createSettlement(@PathVariable Long billingId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.createSettlement(billingId));
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<SettlementResponse>> getHistory(@PathVariable Long billingId) {
        return ResponseEntity.ok(settlementService.historySettlementPerBill(billingId));
    }
}
