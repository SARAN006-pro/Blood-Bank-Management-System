package com.bloodbank.repository;
import com.bloodbank.model.DailySequence;
import com.bloodbank.model.DailySequenceId;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DailySequenceRepository extends JpaRepository<DailySequence, DailySequenceId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DailySequence s WHERE s.id = :id")
    Optional<DailySequence> findByIdForUpdate(@Param("id") DailySequenceId id);
}
