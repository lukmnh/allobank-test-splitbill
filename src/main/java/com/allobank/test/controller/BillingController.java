package com.allobank.test.controller;

import com.allobank.test.dto.billing.BillingCreateRequest;
import com.allobank.test.dto.billing.BillingCreateResponse;
import com.allobank.test.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<BillingCreateResponse> createBilling(@RequestBody BillingCreateRequest billingCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createBillingGroup(billingCreateRequest));
    }
}
