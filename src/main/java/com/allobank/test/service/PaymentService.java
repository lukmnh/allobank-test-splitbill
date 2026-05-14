package com.allobank.test.service;

import com.allobank.test.dto.payment.PaymentRequest;
import com.allobank.test.dto.payment.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse recordPayment(Long billingId, PaymentRequest request);
    List<PaymentResponse> list(Long billingId);
}
