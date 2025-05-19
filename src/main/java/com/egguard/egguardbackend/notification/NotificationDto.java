package com.egguard.egguardbackend.notification;

import com.egguard.egguardbackend.shared.enums.NotificationSeverity;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private Long farmId;
    private NotificationSeverity severity;
    private String message;
    private String photoUrl;
    private LocalDateTime timestamp;
}
