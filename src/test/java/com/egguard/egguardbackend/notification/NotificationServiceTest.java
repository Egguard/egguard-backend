package com.egguard.egguardbackend.notification;

import com.egguard.egguardbackend.farm.Farm;
import com.egguard.egguardbackend.shared.entity.Robot;
import com.egguard.egguardbackend.shared.enums.NotificationSeverity;
import com.egguard.egguardbackend.farm.FarmRepository;
import com.egguard.egguardbackend.shared.repository.RobotRepository;
import com.egguard.egguardbackend.notification.request.RegisterNotificationRequest;
import com.egguard.egguardbackend.shared.service.IStaticContentUploadService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RobotRepository robotRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private IStaticContentUploadService staticContentUploadService;

    @InjectMocks
    private NotificationService notificationService;

    private Robot testRobot;
    private Notification testNotification;
    private RegisterNotificationRequest registerNotificationRequest;
    private NotificationDto notificationDto;
    private MockMultipartFile mockImageFile;

    @BeforeEach
    void setUp() {
        // Setting up test data
        Farm testFarm = Farm.builder()
                .id(1L)
                .name("Test Farm")
                .build();

        testRobot = Robot.builder()
                .id(1L)
                .farm(testFarm)
                .build();

        registerNotificationRequest = new RegisterNotificationRequest();
        registerNotificationRequest.setMessage("Test notification message");
        registerNotificationRequest.setSeverity(NotificationSeverity.CRITICAL);

        testNotification = Notification.builder()
                .id(1L)
                .message(registerNotificationRequest.getMessage())
                .severity(registerNotificationRequest.getSeverity())
                .farm(testFarm)
                .timestamp(LocalDateTime.now())
                .photoUrl("https://example.com/image.jpg")
                .build();

        notificationDto = NotificationDto.builder()
                .id(testNotification.getId())
                .message(testNotification.getMessage())
                .severity(testNotification.getSeverity())
                .timestamp(testNotification.getTimestamp())
                .photoUrl(testNotification.getPhotoUrl())
                .build();

        // Setup mock image file
        mockImageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("registerNotification() should register a notification successfully without image")
    void registerNotificationShouldRegisterNotificationSuccessfully() throws IOException {
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(modelMapper.map(registerNotificationRequest, Notification.class)).thenReturn(testNotification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(modelMapper.map(testNotification, NotificationDto.class)).thenReturn(notificationDto);

        NotificationDto result = notificationService.registerNotification(1L, registerNotificationRequest, null);

        assertNotNull(result);
        assertEquals(notificationDto.getId(), result.getId());
        assertEquals(notificationDto.getMessage(), result.getMessage());
        assertEquals(notificationDto.getSeverity(), result.getSeverity());
        assertEquals(notificationDto.getPhotoUrl(), result.getPhotoUrl());
    }

    @Test
    @DisplayName("registerNotification should register a notification with image successfully")
    void registerNotificationShouldRegisterNotificationWithImageSuccessfully() throws IOException {
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(modelMapper.map(registerNotificationRequest, Notification.class)).thenReturn(testNotification);
        when(staticContentUploadService.uploadImage(mockImageFile)).thenReturn(notificationDto.getPhotoUrl());
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(modelMapper.map(testNotification, NotificationDto.class)).thenReturn(notificationDto);

        NotificationDto result = notificationService.registerNotification(1L, registerNotificationRequest, mockImageFile);

        assertNotNull(result);
        assertEquals(notificationDto.getId(), result.getId());
        assertEquals(notificationDto.getMessage(), result.getMessage());
        assertEquals(notificationDto.getSeverity(), result.getSeverity());
        assertEquals(notificationDto.getPhotoUrl(), result.getPhotoUrl());
    }

    @Test
    @DisplayName("registerNotification() should throw EntityNotFoundException when robot is not found")
    void registerNotificationShouldThrowExceptionWhenRobotNotFound() {
        when(robotRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            notificationService.registerNotification(99L, registerNotificationRequest, null);
        });
    }

    @Test
    @DisplayName("registerNotification() should throw IllegalStateException when robot has no farm")
    void registerNotificationShouldThrowExceptionWhenRobotHasNoFarm() {
        Robot robotWithoutFarm = Robot.builder()
                .id(2L)
                .farm(null)
                .build();
        
        when(robotRepository.findById(2L)).thenReturn(Optional.of(robotWithoutFarm));

        assertThrows(IllegalStateException.class, () -> {
            notificationService.registerNotification(2L, registerNotificationRequest, null);
        });
    }

    @Test
    @DisplayName("registerNotification() should throw IOException when image upload fails")
    void registerNotificationShouldThrowExceptionWhenImageUploadFails() throws IOException {
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(modelMapper.map(registerNotificationRequest, Notification.class)).thenReturn(testNotification);
        when(staticContentUploadService.uploadImage(mockImageFile)).thenThrow(new IOException("Upload failed"));

        assertThrows(IOException.class, () -> {
            notificationService.registerNotification(1L, registerNotificationRequest, mockImageFile);
        });
    }

    @Test
    @DisplayName("getNotificationsByFarm() should get notifications successfully")
    void getNotificationsByFarmShouldGetNotificationsSuccessfully() {
        List<Notification> notifications = List.of(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(notificationRepository.findByFarmId(1L, pageable)).thenReturn(notificationPage);
        when(modelMapper.map(testNotification, NotificationDto.class)).thenReturn(notificationDto);

        Page<NotificationDto> result = notificationService.getNotificationsByFarm(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(notificationDto, result.getContent().getFirst());
    }

    @Test
    @DisplayName("getNotificationsByFarm() should return empty page when farm has no notifications")
    void getNotificationsByFarmShouldReturnEmptyPageWhenFarmHasNoNotifications() {
        Page<Notification> emptyPage = new PageImpl<>(new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(notificationRepository.findByFarmId(1L, pageable)).thenReturn(emptyPage);

        Page<NotificationDto> result = notificationService.getNotificationsByFarm(1L, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("getNotificationsByFarm() should throw EntityNotFoundException when farm is not found for getNotificationsByFarm")
    void getNotificationsByFarmShouldThrowExceptionWhenFarmNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(farmRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            notificationService.getNotificationsByFarm(99L, pageable);
        });
    }
}