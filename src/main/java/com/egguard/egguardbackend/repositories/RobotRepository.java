package com.egguard.egguardbackend.repositories;

import com.egguard.egguardbackend.entities.Robot;
import com.egguard.egguardbackend.enums.RobotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long> {
    List<Robot> findByFarmId(Long farmId);
    List<Robot> findByFarmIdAndStatus(Long farmId, RobotStatus status);
} 