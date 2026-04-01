package com.bothsann.wallet.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String recipientEmail,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String description
) {}
