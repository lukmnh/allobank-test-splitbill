package com.allobank.test.controller;

import com.allobank.test.dto.billing.BillingCreateRequest;
import com.allobank.test.dto.billing.BillingCreateResponse;
import com.allobank.test.dto.billing.BillingResponse;
import com.allobank.test.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<BillingCreateResponse> createBilling(@RequestBody BillingCreateRequest billingCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createBillingGroup(billingCreateRequest));
    }

    @GetMapping("/{billingId}")
    public ResponseEntity<BillingResponse> detail(@PathVariable Long billingId) {
        return ResponseEntity.ok(billingService.detail(billingId));
    }

    @GetMapping
    public ResponseEntity<List<BillingResponse>> list() {
        return ResponseEntity.ok(billingService.list());
    }
}
