package com.egguard.egguardbackend.shared.repository;

import com.egguard.egguardbackend.shared.entity.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long> {
}