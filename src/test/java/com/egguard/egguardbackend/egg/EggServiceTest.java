package com.egguard.egguardbackend.egg;

import com.egguard.egguardbackend.farm.Farm;
import com.egguard.egguardbackend.shared.entity.Robot;
import com.egguard.egguardbackend.egg.exception.DuplicateEggException;
import com.egguard.egguardbackend.farm.FarmRepository;
import com.egguard.egguardbackend.shared.repository.RobotRepository;
import com.egguard.egguardbackend.egg.request.PickEggsRequest;
import com.egguard.egguardbackend.egg.request.RegisterEggRequest;
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
        Farm testFarm = Farm.builder()
                .id(1L)
                .name("Test Farm")
                .build();

        testRobot = Robot.builder()
                .id(1L)
                .farm(testFarm)
                .build();

        registerEggRequest = RegisterEggRequest.builder()
                        .coordX(10.0)
                        .coordY(20.0)
                        .broken(false)
                        .build();

        testEgg = Egg.builder()
                .id(1L)
                .coordX(10.0)
                .coordY(20.0)
                .broken(false)
                .picked(false)
                .farm(testFarm)
                .timestamp(LocalDateTime.now().minusMinutes(5L))
                .build();
        
        eggDto = EggDto.builder()
                .id(1L)
                .farmId(1L)
                .coordX(10.0)
                .coordY(20.0)
                .broken(false)
                .picked(false)
                .timestamp(testEgg.getTimestamp())
                .build();
    }

    @Test
    @DisplayName("registerEgg() should register a new egg successfully")
    void registerEggShouldRegisterSuccessfully() {
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(new ArrayList<>());
        when(modelMapper.map(registerEggRequest, Egg.class)).thenReturn(testEgg);
        when(eggRepository.save(any(Egg.class))).thenReturn(testEgg);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        EggDto result = eggService.registerEgg(1L, registerEggRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(10.0, result.getCoordX());
        assertEquals(20.0, result.getCoordY());
        assertFalse(result.getPicked());
        assertFalse(result.getBroken());
    }

    @Test
    @DisplayName("registerEgg() should throw EntityNotFoundException when robot is not found")
    void registerEggShouldThrowExceptionWhenRobotNotFound() {
        when(robotRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            eggService.registerEgg(99L, registerEggRequest);
        });
    }

    @Test
    @DisplayName("registerEgg() should throw IllegalStateException when robot has no farm")
    void registerEggShouldThrowExceptionWhenRobotHasNoFarm() {
        Robot robotWithoutFarm = Robot.builder()
                .id(2L)
                .farm(null)
                .build();
        
        when(robotRepository.findById(2L)).thenReturn(Optional.of(robotWithoutFarm));

        assertThrows(IllegalStateException.class, () -> {
            eggService.registerEgg(2L, registerEggRequest);
        });
    }

    @Test
    @DisplayName("registerEgg() should throw DuplicateEggException when the egg already exists")
    void registerEggShouldThrowExceptionWhenEggIsDuplicate() {
        List<Egg> existingEggs = new ArrayList<>();
        existingEggs.add(testEgg);
        
        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(existingEggs);
        assertThrows(DuplicateEggException.class, () -> {
            eggService.registerEgg(1L, registerEggRequest);
        });
    }

    @Test
    @DisplayName("registerEgg() should throw DuplicateEggException when the egg already with a small location offset")
    void registerEggShouldThrowExceptionWhenEggIsDuplicateWithSmallLocationOffset() {
        List<Egg> existingEggs = new ArrayList<>();
        existingEggs.add(testEgg);

        testEgg.setCoordX(testEgg.getCoordX() + 0.001);
        testEgg.setCoordY(testEgg.getCoordY() + 0.001);

        when(robotRepository.findById(1L)).thenReturn(Optional.of(testRobot));
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(existingEggs);

        assertThrows(DuplicateEggException.class, () -> {
            eggService.registerEgg(1L, registerEggRequest);
        });
    }


    @Test
    @DisplayName("getEggsByFarm() should get eggs by farm when only farm id is provided")
    void getEggsByFarmShouldGetEggsSuccessfully() {
        List<Egg> eggs = List.of(testEgg);
        List<EggDto> expectedDtos = List.of(eggDto);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmId(1L)).thenReturn(eggs);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        List<EggDto> result = eggService.getEggsByFarm(1L, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDtos, result);
    }

    @Test
    @DisplayName("getEggsByFarm() should get eggs succesfully by picked status")
    void getEggsByFarmShouldGetEggsSuccessfullyByPickedStatus() {
        List<Egg> eggs = List.of(testEgg);
        List<EggDto> expectedDtos = List.of(eggDto);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(eggs);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        List<EggDto> result = eggService.getEggsByFarm(1L, false, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDtos, result);
    }

    @Test
    @DisplayName("getEggsByFarm() should get eggs succesfully by date")
    void getEggsByFarmShouldGetEggsSuccesfullyByDate() {
        List<Egg> eggs = List.of(testEgg);
        List<EggDto> expectedDtos = List.of(eggDto);
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndTimestampBetween(1L, startOfDay, endOfDay)).thenReturn(eggs);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        List<EggDto> result = eggService.getEggsByFarm(1L, null, date);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDtos, result);
    }

    @Test
    @DisplayName("getEggsByFarm() should get eggs succesfully by picked status and date")
    void getEggsByFarmShouldGetEggsByFarmIdPickedStatusAndDateSuccessfully() {
        List<Egg> eggs = List.of(testEgg);
        List<EggDto> expectedDtos = List.of(eggDto);
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPickedAndTimestampBetween(1L, false, startOfDay, endOfDay)).thenReturn(eggs);
        when(modelMapper.map(testEgg, EggDto.class)).thenReturn(eggDto);

        List<EggDto> result = eggService.getEggsByFarm(1L, false, date);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDtos, result);
    }

    @Test
    @DisplayName("getEggsByFarm() should throw EntityNotFoundException when farm is not found")
    void getEggsByFarmShouldThrowExceptionWhenFarmNotFound() {
        when(farmRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            eggService.getEggsByFarm(99L, null, null);
        });
    }

    @Test
    @DisplayName("markEggsAsPicked() should mark eggs as picked successfully when request provided")
    void markEggsAsPickedShouldMarkEggsAsPickedSuccessfullyWhenRequestProvided() {
        List<Egg> eggsToPick = new ArrayList<>();
        eggsToPick.add(testEgg);
        LocalDateTime beforeTime = LocalDateTime.now();
        PickEggsRequest request = new PickEggsRequest();
        request.setBefore(beforeTime);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(eggsToPick);
        
        eggService.markEggsAsPicked(1L, request);

        assertTrue(testEgg.getPicked());
    }

    @Test
    @DisplayName("markEggsAsPicked() should mark eggs as picked using current time when request not provided")
    void markEggsAsPickedShouldMarkEggsSuccessfullyWhenRequestNotProvided() {
        List<Egg> eggsToPick = new ArrayList<>();
        eggsToPick.add(testEgg);
        
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPicked(1L, false)).thenReturn(eggsToPick);
        
        eggService.markEggsAsPicked(1L, null);

        assertTrue(testEgg.getPicked());
    }

    @Test
    @DisplayName("markEggsAsPicked() should throw EntityNotFoundException when farm is not found")
    void markEggsAsPickedShouldThrowExceptionWhenFarmNotFound() {
        when(farmRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            eggService.markEggsAsPicked(99L, new PickEggsRequest());
        });
    }
}