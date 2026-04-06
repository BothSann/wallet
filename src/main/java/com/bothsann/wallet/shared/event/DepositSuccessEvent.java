package com.bothsann.wallet.shared.event;

import java.math.BigDecimal;

public record DepositSuccessEvent(
        String recipientEmail,
        String recipientName,
        BigDecimal amount,
        BigDecimal newBalance
) {}
