package com.bloodbank.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(
        @NotBlank(message = "reason is required")
        String reason
) {
}
