package com.egguard.egguardbackend.notification.request;

import com.egguard.egguardbackend.shared.enums.NotificationSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterNotificationRequest {

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 1000, message = "Message length must be less than or equal to 1000 characters")
    private String message;

    @NotNull(message = "Severity must be provided")
    private NotificationSeverity severity;
}
