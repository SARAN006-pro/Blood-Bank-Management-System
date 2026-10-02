package com.bloodbank.dto;


import java.util.List;

public record DashboardResponse(
        List<StockSummaryRow> stockByGroup,
        long pendingRequests,
        long emergencyPending,
        long nearExpiryCount,
        long todaysDonations,
        long issuedThisMonth,
        long expiredThisMonth
) {
}
