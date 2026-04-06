package com.bothsann.wallet.shared.exception;

import java.math.BigDecimal;

public class DailyLimitCapExceededException extends RuntimeException {
    public DailyLimitCapExceededException(BigDecimal maxLimit) {
        super("Daily limit cannot exceed the maximum allowed limit of " + maxLimit);
    }
}
