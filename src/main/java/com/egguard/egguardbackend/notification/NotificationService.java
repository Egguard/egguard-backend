package com.egguard.egguardbackend.notification;

import com.egguard.egguardbackend.farm.Farm;
import com.egguard.egguardbackend.shared.entity.Robot;
import com.egguard.egguardbackend.farm.FarmRepository;
import com.egguard.egguardbackend.shared.repository.RobotRepository;
import com.egguard.egguardbackend.notification.request.RegisterNotificationRequest;
import com.egguard.egguardbackend.shared.service.IStaticContentUploadService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final RobotRepository robotRepository;
    private final FarmRepository farmRepository;
    private final ModelMapper modelMapper;
    private final IStaticContentUploadService cloudinaryService;

    @Override
    @Transactional
    public NotificationDto registerNotification(Long robotId, RegisterNotificationRequest request, MultipartFile image) throws IOException {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new EntityNotFoundException("Robot not found with id: " + robotId));

        Farm farm = robot.getFarm();
        if (farm == null) {
            throw new IllegalStateException("Robot with id " + robotId + " is not associated with any farm.");
        }

        Notification notification = modelMapper.map(request, Notification.class);
        notification.setFarm(farm);
        
        // Upload image to Cloudinary if provided
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(image);
                notification.setPhotoUrl(imageUrl);
                log.info("Image uploaded successfully for notification, URL: {}", imageUrl);
            } catch (IOException e) {
                log.error("Failed to upload image for notification", e);
                throw e;
            }
        }
        
        Notification savedNotification = notificationRepository.save(notification);
        return modelMapper.map(savedNotification, NotificationDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByFarm(Long farmId, Pageable pageable) {
        if (!farmRepository.existsById(farmId)) {
            throw new EntityNotFoundException("Farm not found with id: " + farmId);
        }

        Page<Notification> notificationPage = notificationRepository.findByFarmId(farmId, pageable);
        return notificationPage.map(notification -> modelMapper.map(notification, NotificationDto.class));
    }
} 