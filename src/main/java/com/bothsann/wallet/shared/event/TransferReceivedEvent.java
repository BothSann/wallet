package com.bothsann.wallet.shared.event;

import java.math.BigDecimal;

public record TransferReceivedEvent(
        String recipientEmail,
        String recipientName,
        String senderEmail,
        BigDecimal amount,
        BigDecimal newBalance
) {}
