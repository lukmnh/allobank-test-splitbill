package com.allobank.test.util;

import com.allobank.test.constant.ErrorCode;
import com.allobank.test.constant.SplitStrategyEnum;
import com.allobank.test.dto.expense.ExpenseCreateRequest;
import com.allobank.test.dto.expense.SplitShareExpenseRequest;
import com.allobank.test.entity.Expense;
import com.allobank.test.entity.ExpenseShare;
import com.allobank.test.entity.Participant;
import com.allobank.test.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SplitStrategyUtil {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    public static List<ExpenseShare> computeEqualShares(Expense expense, ExpenseCreateRequest req,
                                                        Map<Long, Participant> map) {
        int count = req.getSplits().size();
        BigDecimal base = expense.getAmount().divide(BigDecimal.valueOf(count), 4, RoundingMode.DOWN);
        BigDecimal remainder = expense.getAmount().subtract(base.multiply(BigDecimal.valueOf(count)));
        BigDecimal unit = new BigDecimal("0.0001");

        List<ExpenseShare> shares = new ArrayList<>();
        for (SplitShareExpenseRequest entry : req.getSplits()) {
            BigDecimal amount = base;
            if (remainder.compareTo(BigDecimal.ZERO) > 0) {
                amount = amount.add(unit);
                remainder = remainder.subtract(unit);
            }
            shares.add(buildShare(expense, map.get(entry.getParticipantId()), amount, null));
        }
        return shares;
    }

    public static List<ExpenseShare> computePercentageShares(Expense expense, ExpenseCreateRequest req,
                                                       Map<Long, Participant> map) {
        requireSharesPresent(req, SplitStrategyEnum.PERCENTAGE);

        BigDecimal totalPct = req.getSplits().stream()
                .map(SplitShareExpenseRequest::getShare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(totalPct.subtract(HUNDRED).abs().compareTo(TOLERANCE) > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_SPLIT_DATA,
                    String.format("Percentage shares must sum to 100. Got: %s", totalPct.toPlainString())
            );
        }

        List<ExpenseShare> shares = new ArrayList<>();
        BigDecimal running = BigDecimal.ZERO;
        List<SplitShareExpenseRequest> entries = req.getSplits();

        for (int i = 0; i < entries.size(); i++) {
            SplitShareExpenseRequest entry = entries.get(i);
            BigDecimal pct = entry.getShare().setScale(4, RoundingMode.HALF_UP);
            BigDecimal amount;

            if (i == entries.size() - 1) {
                amount = expense.getAmount().subtract(running).setScale(2, RoundingMode.HALF_UP);
            } else {
                amount = expense.getAmount()
                        .multiply(pct)
                        .divide(HUNDRED, 4, RoundingMode.HALF_UP);
                running = running.add(amount);
            }
            shares.add(buildShare(expense, map.get(entry.getParticipantId()), amount, pct));
        }
        return shares;
    }


    public static List<ExpenseShare> computeExactShares(Expense expense, ExpenseCreateRequest req,
                                                  Map<Long, Participant> map) {
        requireSharesPresent(req, SplitStrategyEnum.EXACT);

        BigDecimal sum = req.getSplits().stream()
                .map(SplitShareExpenseRequest::getShare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.subtract(expense.getAmount()).abs().compareTo(TOLERANCE) > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_SPLIT_DATA,
                    String.format("Exact shares must sum to %s. Got: %s",
                            expense.getAmount().toPlainString(),
                            sum.toPlainString())
            );
        }

        return req.getSplits().stream()
                .map(e -> buildShare(expense, map.get(e.getParticipantId()),
                        e.getShare().setScale(2, RoundingMode.HALF_UP), null))
                .toList();
    }

    private static void requireSharesPresent(ExpenseCreateRequest req, SplitStrategyEnum strategy) {
        boolean anyNull = req.getSplits().stream().anyMatch(e -> e.getShare() == null);
        if (anyNull) {
            throw new BusinessException(
                    ErrorCode.INVALID_SPLIT_DATA,
                    String.format("All split entries must include 'share' for strategy: %s", strategy)
            );
        }
    }

    private static ExpenseShare buildShare(Expense expense, Participant participant,
                                    BigDecimal amount, BigDecimal pct) {
        return ExpenseShare.builder()
                .expense(expense)
                .participant(participant)
                .amount(amount)
                .percentageShare(pct)
                .build();
    }
}
