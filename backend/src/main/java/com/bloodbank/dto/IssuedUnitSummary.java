package com.bloodbank.dto;

import java.time.LocalDate;

public record IssuedUnitSummary(
        String unitNumber,
        String bloodGroup,
        LocalDate expiryDate
) {
}
