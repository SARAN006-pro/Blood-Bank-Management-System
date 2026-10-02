package com.bloodbank.repository;
import com.bloodbank.model.BloodCompatibility;
import com.bloodbank.model.BloodCompatibilityId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompatibilityRepository extends JpaRepository<BloodCompatibility, BloodCompatibilityId> {

    @Query("SELECT c.id.donorGroupId FROM BloodCompatibility c WHERE c.id.recipientGroupId = :recipientGroupId")
    List<Long> findDonorGroupIdsByRecipient(@Param("recipientGroupId") Long recipientGroupId);
}
