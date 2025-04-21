package com.egguard.egguardbackend.services;

import com.egguard.egguardbackend.dtos.NotificationDto;
import com.egguard.egguardbackend.entities.Farm;
import com.egguard.egguardbackend.entities.Notification;
import com.egguard.egguardbackend.entities.Robot;
import com.egguard.egguardbackend.repositories.FarmRepository;
import com.egguard.egguardbackend.repositories.NotificationRepository;
import com.egguard.egguardbackend.repositories.RobotRepository;
import com.egguard.egguardbackend.requests.CreateNotificationRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final RobotRepository robotRepository;
    private final FarmRepository farmRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public NotificationDto createNotification(Long robotId, CreateNotificationRequest request) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new EntityNotFoundException("Robot not found with id: " + robotId));

        Farm farm = robot.getFarm();
        if (farm == null) {
            throw new IllegalStateException("Robot with id " + robotId + " is not associated with any farm.");
        }

        Notification notification = modelMapper.map(request, Notification.class);

        //Here we have to extract the attributes, set the FK ids, upload photo somewhere and then save
        notification.setFarm(farm);
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