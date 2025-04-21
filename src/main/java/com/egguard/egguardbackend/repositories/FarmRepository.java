package com.egguard.egguardbackend.repositories;

import com.egguard.egguardbackend.entities.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByUserId(Long userId);
} 