package com.allobank.test.service.impl;

import com.allobank.test.constant.ErrorCode;
import com.allobank.test.dto.expense.ExpenseResponse;
import com.allobank.test.dto.expense.SplitShareExpenseResponse;
import com.allobank.test.dto.payment.PaymentResponse;
import com.allobank.test.dto.settlement.SettlementResponse;
import com.allobank.test.dto.settlementtransaction.SettlementTransactionResponse;
import com.allobank.test.entity.*;
import com.allobank.test.exception.BusinessException;
import com.allobank.test.repository.BillingRepository;
import com.allobank.test.repository.ExpenseRepository;
import com.allobank.test.repository.ParticipantRepository;
import com.allobank.test.repository.SettlementRepository;
import com.allobank.test.service.PaymentService;
import com.allobank.test.service.SettlementService;
import com.allobank.test.util.ServiceCharge;
import com.allobank.test.util.SettlementUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementServiceImpl implements SettlementService {

    private final BillingRepository billingRepository;
    private final ExpenseRepository expenseRepository;
    private final ParticipantRepository participantRepository;
    private final SettlementRepository settlementRepository;
    private final PaymentService paymentService;

    @Transactional
    @Override
    public SettlementResponse createSettlement(Long billingId) {
        Billing billing = billingRepository.findById(billingId).orElseThrow(
                () -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        String.format("Billing id %d not found", billingId)
                )
        );

        List<Expense> expenses = expenseRepository.findByBillingIdWithShares(billingId);
        List<Participant> participants = participantRepository.findByBillingId(billingId);
        List<Payment> payments = paymentService.findByBillingId(billingId);

        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        int serviceChargePct = ServiceCharge.calculatePct();
        BigDecimal serviceChargeAmount = ServiceCharge.calculateAmount(totalExpenses);

        BigDecimal grandTotal = totalExpenses
                .add(serviceChargeAmount)
                .setScale(2, RoundingMode.HALF_UP);

        Settlement settlement = Settlement.builder()
                .billing(billing)
                .totalExpenses(totalExpenses)
                .grandTotal(grandTotal)
                .transactions(new ArrayList<>())
                .build();

        settlement = settlementRepository.save(settlement);

        Map<String, BigDecimal> netBalances = SettlementUtil.buildNetBalances(participants, expenses, payments);

        Map<String, Participant> participantsByName = participants.stream()
                .collect(Collectors.toMap(Participant::getName, p -> p));

        List<SettlementTransaction> transactions = SettlementUtil.computeTransactions(
                netBalances, settlement, participantsByName);

        settlement.getTransactions().addAll(transactions);
        settlement = settlementRepository.save(settlement);

        return SettlementResponse.builder()
                .settlementId(String.valueOf(settlement.getId()))
                .billingName(billing.getName())
                .serviceChargePct(String.valueOf(serviceChargePct))
                .grandTotal(grandTotal.setScale(2, RoundingMode.HALF_UP))
                .build();
    }


    @Transactional(readOnly = true)
    @Override
    public List<SettlementResponse> historySettlementPerBill(Long billingId) {
        billingRepository.findById(billingId).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND,
                        String.format("Billing id %d not found", billingId))
        );

        List<Settlement> settlements = settlementRepository.findByBillingIdWithTransactions(billingId);

        if (settlements.isEmpty()) {
            return List.of();
        }

        List<Expense> expenses = expenseRepository.findByBillingIdWithShares(billingId);
        List<Payment> payments = paymentService.findByBillingId(billingId);
        List<Participant> participants = participantRepository.findByBillingId(billingId);

        int serviceChargePct = ServiceCharge.calculatePct();

        BigDecimal serviceChargeAmount = ServiceCharge.calculateAmount(settlements.getFirst().getTotalExpenses());
        Map<String, BigDecimal> netBalances = SettlementUtil.buildNetBalances(participants, expenses, payments);

        return settlements.stream()
                .map(s -> toResponse(s, netBalances, expenses, payments, serviceChargePct, serviceChargeAmount))
                .toList();
    }

    private SettlementResponse toResponse(Settlement s,
                                          Map<String, BigDecimal> netBalances,
                                          List<Expense> expenses,
                                          List<Payment> payments,
                                          int serviceChargePct,
                                          BigDecimal serviceChargeAmount) {
        return SettlementResponse.builder()
                .settlementId(String.valueOf(s.getId()))
                .billingName(s.getBilling().getName())
                .totalExpenses(s.getTotalExpenses().setScale(2, RoundingMode.HALF_UP))
                .serviceChargePct(String.valueOf(serviceChargePct))
                .serviceChargeAmount(serviceChargeAmount.setScale(2, RoundingMode.HALF_UP))
                .grandTotal(s.getGrandTotal().setScale(2, RoundingMode.HALF_UP))
                .balances(netBalances)
                .transactions(s.getTransactions().stream()
                        .map(t -> SettlementTransactionResponse.builder()
                                .fromParticipantName(t.getFromParticipant().getName())
                                .toParticipantName(t.getToParticipant().getName())
                                .amount(t.getAmount())
                                .build())
                        .toList())
                .categoryBreakdown(SettlementUtil.buildCategoryBreakdown(expenses))
                .expenses(mapToExpenseResponses(expenses))
                .payments(mapToPaymentResponses(payments))
                .settledAt(String.valueOf(s.getCreatedAt()))
                .build();
    }

    private List<ExpenseResponse> mapToExpenseResponses(List<Expense> expenses) {
        return expenses.stream()
                .map(this::mapToExpenseResponse)
                .toList();
    }

    private ExpenseResponse mapToExpenseResponse(Expense e) {
        return ExpenseResponse.builder()
                .description(e.getDescription())
                .amount(e.getAmount())
                .paidByName(e.getPaidBy().getName())
                .splitStrategy(e.getSplitStrategy())
                .shares(e.getShares().stream()
                        .map(share -> SplitShareExpenseResponse.builder()
                                .participantName(share.getParticipant().getName())
                                .amount(share.getAmount())
                                .build())
                        .toList())
                .build();
    }

    private List<PaymentResponse> mapToPaymentResponses(List<Payment> payments) {
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    private PaymentResponse mapToPaymentResponse(Payment p) {
        return PaymentResponse.builder()
                .payerName(p.getPayer().getName())
                .payeeName(p.getPayee().getName())
                .amount(p.getAmount())
                .note(p.getNote())
                .paidAt(p.getPaidAt() != null ? p.getPaidAt().toString() : null)
                .build();
    }
}
