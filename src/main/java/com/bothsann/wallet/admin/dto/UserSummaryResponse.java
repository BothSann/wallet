package com.bothsann.wallet.admin.dto;

import com.bothsann.wallet.shared.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        Role role,
        boolean isActive,
        LocalDateTime createdAt
) {}
