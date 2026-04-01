package com.bothsann.wallet.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$") String phone,
        @NotBlank @Size(min = 8) String password
) {}
