package com.bothsann.wallet.shared.audit;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String entityType,
        UUID entityId,
        String action,
        UUID actorId,
        Object oldValue,
        Object newValue,
        LocalDateTime createdAt
) {
}
