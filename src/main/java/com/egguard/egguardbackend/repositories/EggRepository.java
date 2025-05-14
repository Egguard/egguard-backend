package com.egguard.egguardbackend.repositories;

import com.egguard.egguardbackend.entities.Egg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EggRepository extends JpaRepository<Egg, Long> {
    List<Egg> findByFarmId(Long farmId);
    List<Egg> findByFarmIdAndPicked(Long farmId, Boolean picked);
    List<Egg> findByFarmIdAndPickedAndTimestampBetween(Long farmId, Boolean picked, LocalDateTime start, LocalDateTime end);
    List<Egg> findByFarmIdAndTimestampBetween(Long farmId, LocalDateTime start, LocalDateTime end);
}