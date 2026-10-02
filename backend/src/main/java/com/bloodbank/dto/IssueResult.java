package com.bloodbank.dto;


import java.time.Instant;
import java.util.List;

public record IssueResult(
        Long requestId,
        String status,
        Instant issuedAt,
        List<IssuedUnitSummary> issuedUnits
) {
}
