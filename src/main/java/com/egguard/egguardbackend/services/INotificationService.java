package com.egguard.egguardbackend.services;

import com.egguard.egguardbackend.dtos.NotificationDto;
import com.egguard.egguardbackend.requests.CreateNotificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Interface for Notification service
public interface INotificationService {
    NotificationDto createNotification(Long robotId, CreateNotificationRequest request);
    Page<NotificationDto> getNotificationsByFarm(Long farmId, Pageable pageable);
} 