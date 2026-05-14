package com.allobank.test.service.impl;

import com.allobank.test.dto.payment.PaymentRequest;
import com.allobank.test.dto.payment.PaymentResponse;
import com.allobank.test.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    @Override
    public PaymentResponse recordPayment(Long billingId, PaymentRequest request) {
        return null;
    }

    @Override
    public List<PaymentResponse> list(Long billingId) {
        return List.of();
    }
}
