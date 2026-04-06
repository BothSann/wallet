package com.bothsann.wallet.wallet.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DailyLimitResponse(
        BigDecimal dailyLimit,
        BigDecimal todaySpend,
        BigDecimal remaining,
        Instant resetAt
) {}
