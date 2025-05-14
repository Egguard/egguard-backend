package com.egguard.egguardbackend.services;

import com.egguard.egguardbackend.dtos.NotificationDto;
import com.egguard.egguardbackend.entities.Farm;
import com.egguard.egguardbackend.entities.Notification;
import com.egguard.egguardbackend.entities.Robot;
import com.egguard.egguardbackend.enums.NotificationSeverity;
import com.egguard.egguardbackend.repositories.FarmRepository;
import com.egguard.egguardbackend.repositories.NotificationRepository;
import com.egguard.egguardbackend.repositories.RobotRepository;
import com.egguard.egguardbackend.requests.RegisterNotificationRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
    private CloudinaryService cloudinaryService;

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
                .message("Test notification message")
                .severity(NotificationSeverity.CRITICAL)
                .farm(testFarm)
                .timestamp(LocalDateTime.now())
                .photoUrl("https://example.com/image.jpg")
                .build();

        notificationDto = new NotificationDto();
        notificationDto.setId(1L);
        notificationDto.setMessage("Test notification message");
        notificationDto.setSeverity(NotificationSeverity.CRITICAL);
        notificationDto.setTimestamp(testNotification.getTimestamp());
        notificationDto.setPhotoUrl("https://example.com/image.jpg");

        // Setup mock image file
        mockImageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("Should register a notification successfully without image")
    void shouldRegisterNotificationSuccessfully() throws IOException {
        // Arrange
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(modelMapper.map(registerNotificationRequest, Notification.class)).thenReturn(testNotification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(modelMapper.map(testNotification, NotificationDto.class)).thenReturn(notificationDto);

        // Act
        NotificationDto result = notificationService.registerNotification(1L, registerNotificationRequest, null);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test notification message", result.getMessage());
        assertEquals(NotificationSeverity.CRITICAL, result.getSeverity());
        assertEquals("https://example.com/image.jpg", result.getPhotoUrl());
        
        verify(robotRepository).findById(1L);
        verify(notificationRepository).save(testNotification);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    @DisplayName("Should register a notification with image successfully")
    void shouldRegisterNotificationWithImageSuccessfully() throws IOException {
        // Arrange
        String uploadedImageUrl = "https://cloudinary.com/uploaded-image.jpg";
        
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(modelMapper.map(registerNotificationRequest, Notification.class)).thenReturn(testNotification);
        when(cloudinaryService.uploadImage(mockImageFile)).thenReturn(uploadedImageUrl);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(modelMapper.map(testNotification, NotificationDto.class)).thenReturn(notificationDto);

        // Act
        NotificationDto result = notificationService.registerNotification(1L, registerNotificationRequest, mockImageFile);

        // Assert
        assertNotNull(result);
        
        // Verify the notification has the photo URL set from Cloudinary
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();
        assertEquals(uploadedImageUrl, savedNotification.getPhotoUrl());
        
        verify(robotRepository).findById(1L);
        verify(cloudinaryService).uploadImage(mockImageFile);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when robot is not found")
    void shouldThrowExceptionWhenRobotNotFound() {
        // Arrange
        when(robotRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            notificationService.registerNotification(99L, registerNotificationRequest, null);
        });
        
        verify(robotRepository).findById(99L);
        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when robot has no farm")
    void shouldThrowExceptionWhenRobotHasNoFarm() {
        // Arrange
        Robot robotWithoutFarm = Robot.builder()
                .id(2L)
                .farm(null)
                .build();
        
        when(robotRepository.findById(2L)).thenReturn(Optional.of(robotWithoutFarm));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            notificationService.registerNotification(2L, registerNotificationRequest, null);
        });
        
        verify(robotRepository).findById(2L);
        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    @DisplayName("Should throw IOException when image upload fails")
    void shouldThrowExceptionWhenImageUploadFails() throws IOException {
        // Arrange
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(modelMapper.map(registerNotificationRequest, Notification.class)).thenReturn(testNotification);
        when(cloudinaryService.uploadImage(mockImageFile)).thenThrow(new IOException("Upload failed"));

        // Act & Assert
        assertThrows(IOException.class, () -> {
            notificationService.registerNotification(1L, registerNotificationRequest, mockImageFile);
        });
        
        verify(robotRepository).findById(1L);
        verify(cloudinaryService).uploadImage(mockImageFile);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    @DisplayName("Should get notifications by farm successfully")
    void shouldGetNotificationsByFarmSuccessfully() {
        // Arrange
        List<Notification> notifications = List.of(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(notificationRepository.findByFarmId(1L, pageable)).thenReturn(notificationPage);
        when(modelMapper.map(testNotification, NotificationDto.class)).thenReturn(notificationDto);

        // Act
        Page<NotificationDto> result = notificationService.getNotificationsByFarm(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(notificationDto, result.getContent().getFirst());
        
        verify(farmRepository).existsById(1L);
        verify(notificationRepository).findByFarmId(1L, pageable);
    }

    @Test
    @DisplayName("Should return empty page when farm has no notifications")
    void shouldReturnEmptyPageWhenFarmHasNoNotifications() {
        // Arrange
        Page<Notification> emptyPage = new PageImpl<>(new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(notificationRepository.findByFarmId(1L, pageable)).thenReturn(emptyPage);

        // Act
        Page<NotificationDto> result = notificationService.getNotificationsByFarm(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        
        verify(farmRepository).existsById(1L);
        verify(notificationRepository).findByFarmId(1L, pageable);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when farm is not found for getNotificationsByFarm")
    void shouldThrowExceptionWhenFarmNotFoundForGetNotificationsByFarm() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(farmRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            notificationService.getNotificationsByFarm(99L, pageable);
        });
        
        verify(farmRepository).existsById(99L);
        verifyNoInteractions(notificationRepository);
    }
}