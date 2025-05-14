package com.egguard.egguardbackend.services;

import com.egguard.egguardbackend.dtos.EggDto;
import com.egguard.egguardbackend.entities.Egg;
import com.egguard.egguardbackend.entities.Farm;
import com.egguard.egguardbackend.entities.Robot;
import com.egguard.egguardbackend.exceptions.DuplicateEggException;
import com.egguard.egguardbackend.repositories.EggRepository;
import com.egguard.egguardbackend.repositories.FarmRepository;
import com.egguard.egguardbackend.repositories.RobotRepository;
import com.egguard.egguardbackend.requests.PickEggsRequest;
import com.egguard.egguardbackend.requests.RegisterEggRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EggServiceTest {

    @Mock
    private EggRepository eggRepository;

    @Mock
    private RobotRepository robotRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EggService eggService;

    private Robot testRobot;
    private Egg testEgg;
    private RegisterEggRequest registerEggRequest;
    private EggDto eggDto;

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

        registerEggRequest = new RegisterEggRequest();
        registerEggRequest.setCoordX(10.0);
        registerEggRequest.setCoordY(20.0);
        registerEggRequest.setBroken(false);

        testEgg = Egg.builder()
                .id(1L)
                .coordX(10.0)
                .coordY(20.0)
                .broken(false)
                .picked(false)
                .farm(testFarm)
                .timestamp(LocalDateTime.now().minusMinutes(5L))
                .build();
        
        eggDto = new EggDto();
        eggDto.setId(1L);
        eggDto.setCoordX(10.0);
        eggDto.setCoordY(20.0);
        eggDto.setBroken(false);
        eggDto.setPicked(false);
        eggDto.setTimestamp(testEgg.getTimestamp());
    }

    @Test
    @DisplayName("Should register a new egg successfully")
    void shouldRegisterEggSuccessfully() {
        // Arrange
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(new ArrayList<>());
        when(modelMapper.map(registerEggRequest, Egg.class)).thenReturn(testEgg);
        when(eggRepository.save(any(Egg.class))).thenReturn(testEgg);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        // Act
        EggDto result = eggService.registerEgg(1L, registerEggRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(10.0, result.getCoordX());
        assertEquals(20.0, result.getCoordY());
        assertFalse(result.getPicked());
        assertFalse(result.getBroken());
        
        verify(robotRepository).findById(1L);
        verify(eggRepository).findByFarmIdAndPicked(1L, false);
        verify(eggRepository).save(any(Egg.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when robot is not found")
    void shouldThrowExceptionWhenRobotNotFound() {
        // Arrange
        when(robotRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            eggService.registerEgg(99L, registerEggRequest);
        });
        
        verify(robotRepository).findById(99L);
        verifyNoInteractions(eggRepository);
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
            eggService.registerEgg(2L, registerEggRequest);
        });
        
        verify(robotRepository).findById(2L);
        verifyNoInteractions(eggRepository);
    }

    @Test
    @DisplayName("Should throw DuplicateEggException when egg already exists at location")
    void shouldThrowExceptionWhenEggIsDuplicate() {
        // Arrange
        List<Egg> existingEggs = new ArrayList<>();
        existingEggs.add(testEgg);
        
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(existingEggs);

        // Act & Assert
        assertThrows(DuplicateEggException.class, () -> {
            eggService.registerEgg(1L, registerEggRequest);
        });
        
        verify(robotRepository).findById(1L);
        verify(eggRepository).findByFarmIdAndPicked(1L, false);
        verify(eggRepository, never()).save(any(Egg.class));
    }

    @Test
    @DisplayName("Should get eggs by farm id successfully when only farm id is provided")
    void shouldGetEggsByFarmIdSuccessfully() {
        // Arrange
        List<Egg> eggs = List.of(testEgg);
        List<EggDto> expectedDtos = List.of(eggDto);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmId(1L)).thenReturn(eggs);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        // Act
        List<EggDto> result = eggService.getEggsByFarm(1L, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDtos, result);
        
        verify(farmRepository).existsById(1L);
        verify(eggRepository).findByFarmId(1L);
    }

    @Test
    @DisplayName("Should get eggs by farm id and picked status successfully")
    void shouldGetEggsByFarmIdAndPickedStatusSuccessfully() {
        // Arrange
        List<Egg> eggs = List.of(testEgg);
        List<EggDto> expectedDtos = List.of(eggDto);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(eggs);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        // Act
        List<EggDto> result = eggService.getEggsByFarm(1L, false, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDtos, result);
        
        verify(farmRepository).existsById(1L);
        verify(eggRepository).findByFarmIdAndPicked(1L, false);
    }

    @Test
    @DisplayName("Should get eggs by farm id and date successfully")
    void shouldGetEggsByFarmIdAndDateSuccessfully() {
        // Arrange
        List<Egg> eggs = List.of(testEgg);
        List<EggDto> expectedDtos = List.of(eggDto);
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndTimestampBetween(1L, startOfDay, endOfDay)).thenReturn(eggs);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        // Act
        List<EggDto> result = eggService.getEggsByFarm(1L, null, date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDtos, result);
        
        verify(farmRepository).existsById(1L);
        verify(eggRepository).findByFarmIdAndTimestampBetween(1L, startOfDay, endOfDay);
    }

    @Test
    @DisplayName("Should get eggs by farm id, picked status and date successfully")
    void shouldGetEggsByFarmIdPickedStatusAndDateSuccessfully() {
        // Arrange
        List<Egg> eggs = List.of(testEgg);
        List<EggDto> expectedDtos = List.of(eggDto);
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPickedAndTimestampBetween(1L, false, startOfDay, endOfDay)).thenReturn(eggs);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        // Act
        List<EggDto> result = eggService.getEggsByFarm(1L, false, date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDtos, result);
        
        verify(farmRepository).existsById(1L);
        verify(eggRepository).findByFarmIdAndPickedAndTimestampBetween(1L, false, startOfDay, endOfDay);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when farm is not found for getEggsByFarm")
    void shouldThrowExceptionWhenFarmNotFoundForGetEggsByFarm() {
        // Arrange
        when(farmRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            eggService.getEggsByFarm(99L, null, null);
        });
        
        verify(farmRepository).existsById(99L);
        verifyNoInteractions(eggRepository);
    }

    @Test
    @DisplayName("Should mark eggs as picked successfully")
    void shouldMarkEggsAsPickedSuccessfully() {
        // Arrange
        List<Egg> eggsToPick = new ArrayList<>();
        eggsToPick.add(testEgg);
        LocalDateTime beforeTime = LocalDateTime.now();
        PickEggsRequest request = new PickEggsRequest();
        request.setBefore(beforeTime);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(eggsToPick);
        
        // Act
        eggService.markEggsAsPicked(1L, request);

        // Assert
        assertTrue(testEgg.getPicked());
        verify(farmRepository).existsById(1L);
        verify(eggRepository).findByFarmIdAndPicked(1L, false);
        verify(eggRepository).saveAll(eggsToPick);
    }

    @Test
    @DisplayName("Should mark eggs as picked using current time when no timestamp provided")
    void shouldMarkEggsAsPickedWithCurrentTimeWhenNoTimestampProvided() {
        // Arrange
        List<Egg> eggsToPick = new ArrayList<>();
        eggsToPick.add(testEgg);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(eggsToPick);
        
        // Act
        eggService.markEggsAsPicked(1L, null);

        // Assert
        assertTrue(testEgg.getPicked());
        verify(farmRepository).existsById(1L);
        verify(eggRepository).findByFarmIdAndPicked(1L, false);
        verify(eggRepository).saveAll(eggsToPick);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when farm is not found for markEggsAsPicked")
    void shouldThrowExceptionWhenFarmNotFoundForMarkEggsAsPicked() {
        // Arrange
        when(farmRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            eggService.markEggsAsPicked(99L, new PickEggsRequest());
        });
        
        verify(farmRepository).existsById(99L);
        verifyNoInteractions(eggRepository);
    }
}