package com.allobank.test.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ServiceCharge {
    private static String githubUsername;

    @Value("${app.github-username}")
    public void setGithubUsername(String githubUsername) {
        ServiceCharge.githubUsername = githubUsername;
    }

    public static int calculatePct() {
        if (githubUsername == null || githubUsername.isBlank()) {
            return 0;
        }
        int sum = githubUsername.toLowerCase().chars().sum();
        return sum % 10;
    }
    public static BigDecimal calculateAmount(BigDecimal totalExpenses) {
        if (totalExpenses == null || totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        int pct = calculatePct();
        return totalExpenses
                .multiply(BigDecimal.valueOf(pct))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
