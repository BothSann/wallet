package com.bothsann.wallet.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateDailyLimitRequest(
        @NotNull(message = "Daily limit is required")
        @DecimalMin(value = "0.01", message = "Daily limit must be at least 0.01")
        BigDecimal dailyLimit
) {}
