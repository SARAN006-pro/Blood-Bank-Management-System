package com.bloodbank.dto;

import com.bloodbank.model.BloodGroup;

public record BloodGroupResponse(Long id, String code) {
    public static BloodGroupResponse from(BloodGroup g) {
        return new BloodGroupResponse(g.getId(), g.getCode());
    }
}
