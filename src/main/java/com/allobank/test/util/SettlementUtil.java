package com.allobank.test.util;

import com.allobank.test.entity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SettlementUtil {

    private static final BigDecimal EPSILON = new BigDecimal("0.005");

    public static List<SettlementTransaction> computeTransactions(
            Map<String, BigDecimal> netBalances,
            Settlement settlement,
            Map<String, Participant> participantsByName) {

        PriorityQueue<Entry<String, BigDecimal>> creditors = new PriorityQueue<>(
                (a, b) -> b.getValue().compareTo(a.getValue()));

        PriorityQueue<Map.Entry<String, BigDecimal>> debtors = new PriorityQueue<>(
                Comparator.comparingDouble(e -> e.getValue().doubleValue()));

        netBalances.forEach((name, balance) -> {
            BigDecimal rounded = balance.setScale(2, RoundingMode.HALF_UP);
            if (rounded.compareTo(EPSILON) > 0) {
                creditors.offer(Map.entry(name, rounded));
            } else if (rounded.negate().compareTo(EPSILON) > 0) {
                debtors.offer(Map.entry(name, rounded));
            }
        });

        List<SettlementTransaction> transactions = new ArrayList<>();

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Map.Entry<String, BigDecimal> debtor = debtors.poll();
            Map.Entry<String, BigDecimal> creditor = creditors.poll();

            BigDecimal debtAmount = debtor.getValue().negate();
            BigDecimal creditAmount = creditor.getValue();
            BigDecimal transfer = debtAmount.min(creditAmount).setScale(2, RoundingMode.HALF_UP);

            transactions.add(SettlementTransaction.builder()
                    .settlement(settlement)
                    .fromParticipant(participantsByName.get(debtor.getKey()))
                    .toParticipant(participantsByName.get(creditor.getKey()))
                    .amount(transfer)
                    .build());

            BigDecimal remainingDebt = debtAmount.subtract(transfer);
            BigDecimal remainingCredit = creditAmount.subtract(transfer);

            if (remainingDebt.compareTo(EPSILON) > 0) {
                debtors.offer(Map.entry(debtor.getKey(), remainingDebt.negate()));
            }
            if (remainingCredit.compareTo(EPSILON) > 0) {
                creditors.offer(Map.entry(creditor.getKey(), remainingCredit));
            }
        }

        return transactions;
    }

    public static Map<String, BigDecimal> buildNetBalances(List<Participant> participants,
                                                     List<Expense> expenses,
                                                     List<Payment> payments) {
        Map<String, BigDecimal> balances = participants.stream()
                .collect(Collectors.toMap(
                        Participant::getName,
                        p -> BigDecimal.ZERO,
                        (a, b) -> a,
                        LinkedHashMap::new));

        for (Expense expense : expenses) {
            balances.merge(expense.getPaidBy().getName(), expense.getAmount(), BigDecimal::add);
            for (ExpenseShare share : expense.getShares()) {
                balances.merge(share.getParticipant().getName(), share.getAmount().negate(), BigDecimal::add);
            }
        }

        for (Payment payment : payments) {
            balances.merge(payment.getPayer().getName(), payment.getAmount().negate(), BigDecimal::add);
            balances.merge(payment.getPayee().getName(), payment.getAmount(), BigDecimal::add);
        }

        balances.replaceAll((k, v) -> v.setScale(2, RoundingMode.HALF_UP));
        return balances;
    }

    public static Map<String, BigDecimal> buildCategoryBreakdown(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getExpenseType().name(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().setScale(2, RoundingMode.HALF_UP),
                        (a, b) -> a,
                        TreeMap::new));
    }
}
