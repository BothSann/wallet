package com.bothsann.wallet.shared.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient balance. Available: %.2f, Requested: %.2f",
                available, requested));
    }
}
