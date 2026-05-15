package com.allobank.test.service;

import com.allobank.test.dto.payment.PaymentRequest;
import com.allobank.test.dto.payment.PaymentResponse;
import com.allobank.test.entity.Payment;

import java.util.List;

public interface PaymentService {
    PaymentResponse recordPayment(Long billingId, PaymentRequest request);
    List<Payment> findByBillingId(Long billingId);
}
