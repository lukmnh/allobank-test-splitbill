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
import com.allobank.test.entity.projection.ExpenseDetailProjection;
import com.allobank.test.entity.projection.ExpenseShareProjection;
import com.allobank.test.exception.BusinessException;
import com.allobank.test.repository.BillingRepository;
import com.allobank.test.repository.ExpenseRepository;
import com.allobank.test.repository.ExpenseShareRepository;
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
    private final ExpenseShareRepository expenseShareRepository;

    @Transactional
    @Override
    public ExpenseResponse createExpense(Long billingId, ExpenseCreateRequest request) {
        log.info("entering addExpense with billingId {}", billingId);
        if(request.getSplitStrategy().equals(SplitStrategyEnum.EXACT) || request.getSplitStrategy().equals(SplitStrategyEnum.PERCENTAGE)){
            boolean anyNull = request.getSplits().stream()
                    .anyMatch(e -> e.getShare() == null);
            if (anyNull) {
                throw new BusinessException(
                        ErrorCode.FIELD_REQUIRED,
                        String.format("Share is required for strategy: %s", request.getSplitStrategy())
                );
            }
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
        Expense saved = expenseRepository.save(expense);
        return createResponse(saved);
    }

    @Override
    public List<ExpenseResponse> list(Long billingId) {
        Billing billing = billingRepository.findByIdWithParticipants(billingId).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND,
                        String.format("Billing id %d not found", billingId)));
        return expenseRepository.findByBillingId(billingId).stream()
                .map(this::listResponse)
                .toList();
    }

    @Override
    public ExpenseResponse detail(Long billingId, Long expenseId) {
        ExpenseDetailProjection projection = expenseRepository
                .findDetailById(expenseId, billingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "Expense %d not found in billing %d".formatted(expenseId, billingId)));

        List<ExpenseShareProjection> shares = expenseShareRepository
                .findShareProjectionsByExpenseId(expenseId);

        return detailResponse(projection, shares);
    }

    private ExpenseResponse createResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(String.valueOf(e.getId()))
                .description(e.getDescription())
                .amount(e.getAmount())
                .type(e.getExpenseType())
                .splitStrategy(e.getSplitStrategy())
                .createdAt(String.valueOf(e.getCreatedAt()))
                .build();
    }

    private ExpenseResponse listResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(String.valueOf(e.getId()))
                .description(e.getDescription())
                .amount(e.getAmount())
                .type(e.getExpenseType())
                .splitStrategy(e.getSplitStrategy())
                .build();
    }

    private ExpenseResponse detailResponse(ExpenseDetailProjection p,
                                             List<ExpenseShareProjection> shares) {
        return ExpenseResponse.builder()
                .id(String.valueOf(p.getId()))
                .description(p.getDescription())
                .amount(p.getAmount())
                .type(p.getExpenseType())
                .splitStrategy(p.getSplitStrategy())
                .billingId(String.valueOf(p.getBillingId()))
                .billingName(p.getBillingName())
                .paidByName(p.getPaidByName())
                .shares(shares.stream()
                        .map(s -> SplitShareExpenseResponse.builder()
                                .participantName(s.getParticipantName())
                                .amount(s.getAmount())
                                .percentageShare(s.getPercentageShare())
                                .build())
                        .toList())
                .createdAt(String.valueOf(p.getCreatedAt()))
                .build();
    }
}
