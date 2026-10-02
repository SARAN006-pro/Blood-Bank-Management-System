package com.bloodbank.dto;

import com.bloodbank.model.enums.ApprovalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HospitalDecisionRequest(
        ApprovalStatus decision,
        ApprovalStatus status,
        @Size(max = 255) String reason
) {
    public ApprovalStatus effectiveDecision() {
        return decision != null ? decision : status;
    }
}
