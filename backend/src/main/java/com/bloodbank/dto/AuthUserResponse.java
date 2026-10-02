package com.bloodbank.dto;

import com.bloodbank.model.enums.ApprovalStatus;
import com.bloodbank.model.enums.Role;
import com.bloodbank.model.User;

public record AuthUserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        boolean active,
        ApprovalStatus hospitalApprovalStatus
) {
    public static AuthUserResponse from(User u, ApprovalStatus approvalStatus) {
        return new AuthUserResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getRole(),
                u.isActive(),
                u.getRole() == Role.HOSPITAL ? approvalStatus : null
        );
    }
}
