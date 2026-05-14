package com.allobank.test.entity.projection;

import java.math.BigDecimal;

public interface ExpenseShareProjection {
    String getParticipantName();
    BigDecimal getAmount();
    BigDecimal getPercentageShare();
}
