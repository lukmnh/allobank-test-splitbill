package com.allobank.test.service.impl;

import com.allobank.test.constant.ErrorCode;
import com.allobank.test.constant.SplitStrategyEnum;
import com.allobank.test.dto.expense.ExpenseCreateRequest;
import com.allobank.test.dto.expense.ExpenseResponse;
import com.allobank.test.dto.expense.SplitShareExpenseResponse;
import com.allobank.test.entity.Billing;
import com.allobank.test.entity.Expense;
import com.allobank.test.entity.ExpenseShare;
import com.allobank.test.entity.Participant;
import com.allobank.test.exception.BusinessException;
import com.allobank.test.repository.BillingRepository;
import com.allobank.test.repository.ExpenseRepository;
import com.allobank.test.repository.ParticipantRepository;
import com.allobank.test.service.ExpenseService;
import com.allobank.test.util.SplitStrategyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final BillingRepository billingRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    @Override
    public ExpenseResponse createExpense(Long billingId, ExpenseCreateRequest request) {
        log.info("entering addExpense with billingId {}", billingId);
        if(request.getSplitStrategy().equals(SplitStrategyEnum.EXACT) || request.getSplitStrategy().equals(SplitStrategyEnum.PERCENTAGE)){
            throw new BusinessException(
                    ErrorCode.FIELD_REQUIRED,
                    String.format("Share is required for strategy: %s", request.getSplitStrategy())
            );
        }
        Billing billing = billingRepository.findByIdWithParticipants(billingId).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND,
                        "Billing group id %d not found".formatted(billingId))
        );

        Participant paidBy = participantRepository.findByIdAndBillingId(request.getPaidById(), billingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "Participant %d not found in group %d".formatted(request.getPaidById(), billingId))
                );

        Map<Long, Participant> participantMap = participantRepository.findByBillingId(billingId)
                .stream().collect(Collectors.toMap(Participant::getId, Function.identity()));

        request.getSplits().forEach(entry -> {
            if (!participantMap.containsKey(entry.getParticipantId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND,
                        "Participant %d not found in group %d".formatted(request.getPaidById(), billingId));
            }
        });

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount().setScale(4, RoundingMode.HALF_UP))
                .expenseType(request.getType())
                .splitStrategy(request.getSplitStrategy())
                .paidBy(paidBy)
                .billing(billing)
                .shares(new ArrayList<>())
                .build();

        List<ExpenseShare> shares = switch (request.getSplitStrategy()) {
            case EQUAL      -> SplitStrategyUtil.computeEqualShares(expense, request, participantMap);
            case PERCENTAGE -> SplitStrategyUtil.computePercentageShares(expense, request, participantMap);
            case EXACT      -> SplitStrategyUtil.computeExactShares(expense, request, participantMap);
        };

        expense.getShares().addAll(shares);
        return toResponse(expenseRepository.save(expense));
    }

    private ExpenseResponse toResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(String.valueOf(e.getId()))
                .description(e.getDescription())
                .amount(e.getAmount())
                .type(e.getExpenseType())
                .splitStrategy(e.getSplitStrategy())
                .paidById(String.valueOf(e.getPaidBy().getId()))
                .paidByName(e.getPaidBy().getName())
                .shares(e.getShares().stream()
                        .map(s -> SplitShareExpenseResponse.builder()
                                .participantId(String.valueOf(s.getParticipant().getId()))
                                .participantName(s.getParticipant().getName())
                                .amount(s.getAmount())
                                .percentageShare(s.getPercentageShare())
                                .build())
                        .toList())
                .createdAt(String.valueOf(e.getCreatedAt()))
                .build();
    }
}
