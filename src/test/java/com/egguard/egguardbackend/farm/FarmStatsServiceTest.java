package com.egguard.egguardbackend.farm;

import com.egguard.egguardbackend.egg.Egg;
import com.egguard.egguardbackend.egg.EggRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FarmStatsServiceTest {

    @Mock
    private EggRepository eggRepository;

    @Mock
    private FarmRepository farmRepository;

    @InjectMocks
    private FarmStatsService farmStatsService;

    private Egg testEgg;
    private LocalDate from;
    private LocalDate to;
    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;

    @BeforeEach
    void setUp() {
        Farm testFarm = Farm.builder()
                .id(1L)
                .name("Test Farm")
                .build();

        testEgg = Egg.builder()
                .id(1L)
                .picked(false)
                .farm(testFarm)
                .timestamp(fromDateTime)
                .build();

        from = LocalDate.of(2025, 4, 17);
        to = from.plusDays(1);

        fromDateTime = from.atStartOfDay();
        toDateTime = to.atTime(23, 59, 59);
    }

    private List<Egg> createPickedEggsWithVaryingDays() {
        return List.of(
                testEgg.toBuilder().broken(false).timestamp(fromDateTime.plusDays(0)).build(),
                testEgg.toBuilder().broken(false).timestamp(fromDateTime.plusDays(0)).build(),
                testEgg.toBuilder().broken(false).timestamp(fromDateTime.plusDays(1)).build(),
                testEgg.toBuilder().broken(true).timestamp(fromDateTime.plusDays(1)).build(),
                testEgg.toBuilder().broken(false).timestamp(fromDateTime.plusDays(1)).build(),
                testEgg.toBuilder().broken(true).timestamp(fromDateTime.plusDays(1)).build()
        );
    }

    private List<Egg> createPickedEggsSameDay() {
        return List.of(
                testEgg.toBuilder().broken(false).timestamp(fromDateTime).build(),
                testEgg.toBuilder().broken(false).timestamp(fromDateTime).build(),
                testEgg.toBuilder().broken(false).timestamp(fromDateTime).build(),
                testEgg.toBuilder().broken(true).timestamp(fromDateTime).build()
        );
    }

    @Test
    @DisplayName("getFarmStats() should get correct statistics")
    void getFarmStatsShouldGetStatsSuccessfully() {
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPickedAndTimestampBetween(1L, true, fromDateTime, toDateTime))
                .thenReturn(createPickedEggsWithVaryingDays());
        FarmStatsDto result = farmStatsService.getFarmStats(1L, from, to);

        assertNotNull(result);
        assertEquals(6L, result.getTotalPickedEggs());
        assertEquals(2L, result.getAverageNotBrokenEggsPickedPerDay());
        assertEquals(1L, result.getAverageBrokenEggsPickedPerDay());

        double delta = 0.1;
        assertEquals(33.3, result.getBrokenEggsPercentage(), delta);
    }

    @Test
    @DisplayName("getFarmStats() should get correct statistics when there were no eggs")
    void getFarmStatsShouldGetStatsSuccessfullyWhenNoEggs() {
        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPickedAndTimestampBetween(1L, true, fromDateTime, toDateTime))
                .thenReturn(List.of());
        FarmStatsDto result = farmStatsService.getFarmStats(1L, from, to);

        assertNotNull(result);
        assertEquals(0L, result.getTotalPickedEggs());
        assertEquals(0L, result.getAverageNotBrokenEggsPickedPerDay());
        assertEquals(0L, result.getAverageBrokenEggsPickedPerDay());

        double delta = 0.1;
        assertEquals(0.0, result.getBrokenEggsPercentage(), delta);
    }

    @Test
    @DisplayName("getFarmStats() should get correct statistics when the date interval is for the same day")
    void getFarmStatsShouldGetStatsSuccessfullyWhenIntervalIsTheSameDay() {
        to = from;
        toDateTime = from.atTime(23, 59, 59);

        when(farmRepository.existsById(1L)).thenReturn(true);
        when(eggRepository.findByFarmIdAndPickedAndTimestampBetween(1L, true, fromDateTime, toDateTime))
                .thenReturn(createPickedEggsSameDay());
        FarmStatsDto result = farmStatsService.getFarmStats(1L, from, to);

        assertNotNull(result);
        assertEquals(4L, result.getTotalPickedEggs());
        assertEquals(3L, result.getAverageNotBrokenEggsPickedPerDay());
        assertEquals(1L, result.getAverageBrokenEggsPickedPerDay());

        double delta = 0.1;
        assertEquals(25.0, result.getBrokenEggsPercentage(), delta);
    }

    @Test
    @DisplayName("getFarmStats() should throw IllegalStateException when dates range is negative")
    void getFarmStatsShouldThrowExceptionWhenNegativeRange() {
        from = LocalDate.of(2025, 4, 17);
        to = from.minusDays(1);

        assertThrows(IllegalStateException.class, () -> farmStatsService.getFarmStats(1L, from, to));
    }

    @Test
    @DisplayName("getFarmStats() should throw EntityNotFoundException when farm is not found")
    void getFarmStatsShouldThrowExceptionWhenFarmNotFound() {
        when(farmRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            farmStatsService.getFarmStats(99L, from, to);
        });
    }
}
