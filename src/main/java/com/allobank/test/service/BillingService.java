package com.allobank.test.service;

import com.allobank.test.dto.billing.BillingCreateRequest;
import com.allobank.test.dto.billing.BillingCreateResponse;

public interface BillingService {
    BillingCreateResponse createBillingGroup(BillingCreateRequest request);
}
