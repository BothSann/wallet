package com.bothsann.wallet.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePinRequest(
        @NotBlank(message = "Current PIN is required")
        @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
        String currentPin,

        @NotBlank(message = "New PIN is required")
        @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
        String newPin
) {}
