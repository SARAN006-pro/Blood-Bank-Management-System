package com.bloodbank.dto;

import java.util.List;

public record DonorDetailResponse(
        DonorProfileResponse profile,
        EligibilityResponse eligibility,
        List<DonationSummary> donations
) {}
