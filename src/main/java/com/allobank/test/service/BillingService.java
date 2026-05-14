package com.allobank.test.service;

import com.allobank.test.dto.billing.BillingCreateRequest;
import com.allobank.test.dto.billing.BillingCreateResponse;
import com.allobank.test.dto.billing.BillingResponse;

import java.util.List;

public interface BillingService {
    BillingCreateResponse createBillingGroup(BillingCreateRequest request);
    List<BillingResponse> list();
    BillingResponse detail(Long billingId);
}
