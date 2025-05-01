package com.egguard.egguardbackend.controllers;

import com.egguard.egguardbackend.dtos.NotificationDto;
import com.egguard.egguardbackend.requests.CreateNotificationRequest;
import com.egguard.egguardbackend.services.INotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    @PostMapping("/robots/{robot_id}/notifications")
    public ResponseEntity<NotificationDto> createNotificationFromRobot(
            @PathVariable("robot_id") Long robotId,
            @Valid @RequestBody CreateNotificationRequest request) {
        // TODO: Add error handling for Robot not found (404)
        // TODO: Handle potential file upload for 'photo'
        NotificationDto createdNotification = notificationService.createNotification(robotId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
    }

    @GetMapping("/farms/{farm_id}/notifications")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByFarm(
            @PathVariable("farm_id") Long farmId,
            @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        // TODO: Add error handling for Farm not found (404)
        Page<NotificationDto> notifications = notificationService.getNotificationsByFarm(farmId, pageable);
        return ResponseEntity.ok(notifications);
    }
}
