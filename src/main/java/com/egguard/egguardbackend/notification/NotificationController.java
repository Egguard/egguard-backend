package com.egguard.egguardbackend.notification;

import com.egguard.egguardbackend.notification.request.RegisterNotificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final INotificationService notificationService;

    
    @PostMapping(value = "/robots/{robot_id}/notifications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NotificationDto> registerNotification(
            @PathVariable("robot_id") Long robotId,
            @RequestPart("notification") @Valid RegisterNotificationRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            NotificationDto createdNotification = notificationService.registerNotification(robotId, request, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
        } catch (IOException e) {
            log.error("Error processing image upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/farms/{farm_id}/notifications")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByFarm(
            @PathVariable("farm_id") Long farmId,
            @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotificationDto> notifications = notificationService.getNotificationsByFarm(farmId, pageable);
        return ResponseEntity.ok(notifications);
    }
}
