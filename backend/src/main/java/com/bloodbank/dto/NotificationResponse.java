package com.bloodbank.dto;

import com.bloodbank.model.enums.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        boolean read,
        String refType,
        Long refId,
        Instant createdAt
) {
}
