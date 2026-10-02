package com.bloodbank.service;

import com.bloodbank.dto.DonationSummary;
import java.util.List;

public interface DonationQueryPort {
    List<DonationSummary> findByDonorId(Long donorId);
    boolean hasDonations(Long donorId);
}
