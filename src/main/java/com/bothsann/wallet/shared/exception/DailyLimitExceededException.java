package com.bothsann.wallet.shared.exception;

import java.math.BigDecimal;

public class DailyLimitExceededException extends RuntimeException {
    public DailyLimitExceededException(BigDecimal limit, BigDecimal spent, BigDecimal requested) {
        super("Daily limit of " + limit + " exceeded: already spent " + spent
              + ", requested " + requested);
    }
}
