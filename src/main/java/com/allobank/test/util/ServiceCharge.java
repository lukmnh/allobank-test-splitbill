package com.allobank.test.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ServiceCharge {
    public static int calculatePct(String githubUsername) {
        if (githubUsername == null || githubUsername.isBlank()) return 0;
        int sum = githubUsername.toLowerCase().chars().sum();
        return sum % 10;
    }

    public BigDecimal calculateAmount(int pct, BigDecimal totalExpenses) {
        if (pct == 0 || totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return totalExpenses
                .multiply(BigDecimal.valueOf(pct))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }
}
