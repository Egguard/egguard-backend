package com.egguard.egguardbackend.services;

import com.egguard.egguardbackend.dtos.EggDto;
import com.egguard.egguardbackend.requests.RegisterEggRequest;
import com.egguard.egguardbackend.requests.PickEggsRequest;
import com.egguard.egguardbackend.entities.Egg;
import com.egguard.egguardbackend.entities.Farm;
import com.egguard.egguardbackend.entities.Robot;
import com.egguard.egguardbackend.repositories.EggRepository;
import com.egguard.egguardbackend.repositories.FarmRepository;
import com.egguard.egguardbackend.repositories.RobotRepository;
import com.egguard.egguardbackend.utils.MathUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EggService implements IEggService {

    private final EggRepository eggRepository;
    private final RobotRepository robotRepository;
    private final FarmRepository farmRepository;
    private final ModelMapper modelMapper;
    
    /**
     * The threshold distance below which two eggs are considered at the same position
     */
    private static final double DUPLICATE_DISTANCE_THRESHOLD = 1.0;

    @Override
    @Transactional
    public EggDto registerEgg(Long robotId, RegisterEggRequest request) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new EntityNotFoundException("Robot not found with id: " + robotId));
        Farm farm = robot.getFarm();
        if (farm == null) {
            throw new IllegalStateException("Robot with id " + robotId + " is not associated with any farm.");
        }

        List<Egg> unpickedEggs = eggRepository.findByFarmIdAndPicked(farm.getId(), false);
        
        for (Egg existingEgg : unpickedEggs) {
            if (isDuplicate(existingEgg, request)) {
                return null;
            }
        }

        Egg egg = modelMapper.map(request, Egg.class);
        egg.setFarm(farm);
        Egg savedEgg = eggRepository.save(egg);
        return modelMapper.map(savedEgg, EggDto.class);
    }
    
    /**
     * Checks if an existing egg and a new egg request represent the same egg
     * Considers position and status (picked and broken)
     * 
     * @param existingEgg The existing egg from the database
     * @param request The new egg registration request
     * @return true if the eggs are considered duplicates, false otherwise
     */
    private boolean isDuplicate(Egg existingEgg, RegisterEggRequest request) {
        double distance = MathUtils.calculateDistance(
            existingEgg.getCoordX(), existingEgg.getCoordY(),
            request.getCoordX(), request.getCoordY()
        );

        boolean sameLocation = distance <= DUPLICATE_DISTANCE_THRESHOLD;
        boolean sameBrokenState = existingEgg.getBroken() == request.getBroken();

        return sameLocation && sameBrokenState;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EggDto> getEggsByFarm(Long farmId, Boolean picked, LocalDate date) {
        if (!farmRepository.existsById(farmId)) {
            throw new EntityNotFoundException("Farm not found with id: " + farmId);
        }

        List<Egg> eggs;
        if (picked != null && date != null) {
             LocalDateTime startOfDay = date.atStartOfDay();
             LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
             eggs = eggRepository.findByFarmId(farmId).stream()
                     .filter(egg -> picked.equals(egg.getPicked()))
                     .filter(egg -> !egg.getTimestamp().isBefore(startOfDay) && egg.getTimestamp().isBefore(endOfDay))
                     .collect(Collectors.toList());
        } else if (picked != null) {
            eggs = eggRepository.findByFarmIdAndPicked(farmId, picked);
        } else if (date != null) {
             LocalDateTime startOfDay = date.atStartOfDay();
             LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
             eggs = eggRepository.findByFarmId(farmId).stream()
                     .filter(egg -> !egg.getTimestamp().isBefore(startOfDay) && egg.getTimestamp().isBefore(endOfDay))
                     .collect(Collectors.toList());
        } else {
            eggs = eggRepository.findByFarmId(farmId);
        }

        return eggs.stream()
                .map(egg -> modelMapper.map(egg, EggDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markEggsAsPicked(Long farmId, PickEggsRequest pickEggsRequest) {
         if (!farmRepository.existsById(farmId)) {
            throw new EntityNotFoundException("Farm not found with id: " + farmId);
        }

        LocalDateTime beforeTimestamp = (pickEggsRequest != null && pickEggsRequest.getBefore() != null)
                ? pickEggsRequest.getBefore() : LocalDateTime.now(); // Default to now if no time specified

        // Find eggs in the farm that are not picked and optionally before the timestamp
        List<Egg> eggsToPick = eggRepository.findByFarmIdAndPicked(farmId, false).stream()
                .filter(egg -> egg.getTimestamp().isBefore(beforeTimestamp))
                .collect(Collectors.toList());

        if (!eggsToPick.isEmpty()) {
            eggsToPick.forEach(egg -> egg.setPicked(true));
            eggRepository.saveAll(eggsToPick);
        }
    }
}