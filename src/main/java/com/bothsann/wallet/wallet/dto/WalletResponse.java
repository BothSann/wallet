package com.bothsann.wallet.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        BigDecimal balance,
        String currency,
        LocalDateTime updatedAt
) {}
