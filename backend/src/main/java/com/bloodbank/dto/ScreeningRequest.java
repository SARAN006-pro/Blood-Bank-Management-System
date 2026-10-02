package com.bloodbank.dto;

import com.bloodbank.model.enums.ScreeningStatus;
import jakarta.validation.constraints.NotNull;

public record ScreeningRequest(
        @NotNull(message = "status is required")
        ScreeningStatus status,

        String failureReason
) {
}
