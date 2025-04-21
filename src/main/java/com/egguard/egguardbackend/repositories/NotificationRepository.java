package com.egguard.egguardbackend.repositories;

import com.egguard.egguardbackend.entities.Notification;
import com.egguard.egguardbackend.enums.NotificationSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByFarmId(Long farmId, Pageable pageable);
    List<Notification> findByFarmIdAndTimestampAfter(Long farmId, LocalDateTime timestamp);
    Page<Notification> findByFarmIdAndSeverity(Long farmId, NotificationSeverity severity, Pageable pageable);
} 