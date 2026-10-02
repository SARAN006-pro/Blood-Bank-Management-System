package com.bloodbank.dto;

import com.bloodbank.model.enums.ApprovalStatus;
import com.bloodbank.model.Hospital;
import java.time.Instant;

public record HospitalResponse(
        Long id,
        String name,
        String licenseNo,
        String contactPerson,
        String phone,
        String address,
        String city,
        String email,
        ApprovalStatus approvalStatus,
        String decisionReason,
        Instant createdAt
) {
    public static HospitalResponse from(Hospital h) {
        String email = h.getUser() != null ? h.getUser().getEmail() : "";
        return new HospitalResponse(
                h.getId(),
                h.getName(),
                h.getLicenseNo(),
                h.getContactPerson(),
                h.getPhone(),
                h.getAddress(),
                h.getCity(),
                email,
                h.getApprovalStatus(),
                h.getDecisionReason(),
                h.getCreatedAt()
        );
    }
}
