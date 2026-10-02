package com.bloodbank.dto;

import com.bloodbank.model.enums.ApprovalStatus;
import com.bloodbank.model.Hospital;

public record HospitalDecisionResponse(
        Long id,
        String name,
        ApprovalStatus approvalStatus,
        String decisionReason
) {
    public static HospitalDecisionResponse from(Hospital h) {
        return new HospitalDecisionResponse(
                h.getId(),
                h.getName(),
                h.getApprovalStatus(),
                h.getDecisionReason()
        );
    }
}
