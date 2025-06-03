package com.egguard.egguardbackend.notification;

import com.egguard.egguardbackend.notification.request.RegisterNotificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// Interface for Notification service
public interface INotificationService {
    /**
     * Registers a notification with an image
     */
    NotificationDto registerNotification(Long robotId, RegisterNotificationRequest request, MultipartFile image) throws IOException;
    
    /**
     * Gets notifications for a farm with pagination
     */
    Page<NotificationDto> getNotificationsByFarm(Long farmId, Pageable pageable);
} 