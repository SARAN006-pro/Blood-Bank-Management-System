package com.bloodbank.service;
import com.bloodbank.repository.DonationRepository;

import com.bloodbank.dto.DonationSummary;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class DonationQueryAdapter implements DonationQueryPort {

    private final DonationRepository donationRepo;

    public DonationQueryAdapter(DonationRepository donationRepo) {
        this.donationRepo = donationRepo;
    }

    @Override
    public List<DonationSummary> findByDonorId(Long donorId) {
        return donationRepo.findAllByDonorIdOrderByDonationDateDesc(donorId).stream()
                .map(d -> new DonationSummary(
                        d.getId(),
                        d.getDonationDate(),
                        d.getVolumeMl(),
                        d.getScreeningStatus().name(),
                        d.getFailureReason()
                ))
                .toList();
    }

    @Override
    public boolean hasDonations(Long donorId) {
        return donationRepo.countByDonorId(donorId) > 0;
    }
}
