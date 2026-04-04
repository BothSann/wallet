package com.bothsann.wallet.admin.dto;

import com.bothsann.wallet.shared.enums.Role;
import com.bothsann.wallet.user.entity.User;

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
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
