package com.allobank.test.service.impl;

import com.allobank.test.constant.ErrorCode;
import com.allobank.test.dto.payment.PaymentRequest;
import com.allobank.test.dto.payment.PaymentResponse;
import com.allobank.test.entity.Billing;
import com.allobank.test.entity.Participant;
import com.allobank.test.entity.Payment;
import com.allobank.test.exception.BusinessException;
import com.allobank.test.repository.BillingRepository;
import com.allobank.test.repository.ParticipantRepository;
import com.allobank.test.repository.PaymentRepository;
import com.allobank.test.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ParticipantRepository participantRepository;
    private final BillingRepository billingRepository;

    @Override
    public PaymentResponse recordPayment(Long billingId, PaymentRequest request) {
        if (request.getPayerId().equals(request.getPayeeId())) {
            throw new BusinessException(ErrorCode.PAYER_CONFLICT);
        }

        Billing billing = billingRepository.findByIdWithParticipants(billingId).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND,
                        "Billing group id %d not found".formatted(billingId))
        );


        Participant payer = participantRepository.findByIdAndBillingId(request.getPayerId(), billingId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        String.format("Payer with id %s not found in group %s", request.getPayerId(), billingId)
                ));

        Participant payee = participantRepository.findByIdAndBillingId(request.getPayeeId(), billingId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        String.format("Payee with id %s not found in group %s", request.getPayerId(), billingId)
                ));

        Payment payment = Payment.builder()
                .payer(payer)
                .payee(payee)
                .amount(request.getAmount().setScale(2, RoundingMode.HALF_UP))
                .billing(billing)
                .note(request.getNote())
                .paidAt(LocalDateTime.now())
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    @Override
    public List<Payment> findByBillingId(Long billingId) {
        return paymentRepository.findByBillingIdWithParticipants(billingId);
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .payerName(p.getPayer().getName())
                .payeeName(p.getPayee().getName())
                .amount(p.getAmount())
                .note(p.getNote())
                .paidAt(String.valueOf(p.getPaidAt()))
                .build();
    }
}
