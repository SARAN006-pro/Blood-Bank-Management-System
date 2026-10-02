package com.bloodbank.dto;

import jakarta.validation.constraints.NotBlank;

public record DiscardRequest(
        @NotBlank(message = "reason is required")
        String reason
) {
}
