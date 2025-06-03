package com.egguard.egguardbackend.farm;

import com.egguard.egguardbackend.egg.Egg;
import com.egguard.egguardbackend.egg.EggRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmStatsService implements IFarmStatsService {

    private final EggRepository eggRepository;
    private final FarmRepository farmRepository;

    @Override
    @Transactional
    public FarmStatsDto getFarmStats(Long farmId, LocalDate from, LocalDate to) {
        if(from.isAfter(to)){
            throw new IllegalStateException("The dates range can't be negative");
        }

        if(!farmRepository.existsById(farmId)){
            throw new EntityNotFoundException("Farm with id " + farmId + " not found");
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);

        List<Egg> pickedEggs = eggRepository.findByFarmIdAndPickedAndTimestampBetween(
                farmId, true, fromDateTime, toDateTime
        );

        long totalPicked = pickedEggs.size();

        long pickedNotBroken = pickedEggs.stream()
                .filter(egg -> !egg.getBroken())
                .count();

        long pickedBroken = pickedEggs.stream()
                .filter(Egg::getBroken)
                .count();

        long daysBetween = ChronoUnit.DAYS.between(from, to) + 1;

        long avgPickedNotBrokenPerDay = daysBetween == 0 ? 0 : pickedNotBroken / daysBetween;
        long avgPickedBrokenPerDay = daysBetween == 0 ? 0 : pickedBroken / daysBetween;

        double brokenEggsPercentage = totalPicked == 0 ? 0.0 : (double) pickedBroken * 100 / totalPicked;
        brokenEggsPercentage = Math.round(brokenEggsPercentage * 100.0) / 100.0;

        return FarmStatsDto.builder()
                .totalPickedEggs(totalPicked)
                .averageNotBrokenEggsPickedPerDay(avgPickedNotBrokenPerDay)
                .averageBrokenEggsPickedPerDay(avgPickedBrokenPerDay)
                .brokenEggsPercentage(brokenEggsPercentage)
                .build();
    }
}
