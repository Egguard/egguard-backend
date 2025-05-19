package com.egguard.egguardbackend.egg;

import com.egguard.egguardbackend.egg.request.RegisterEggRequest;
import com.egguard.egguardbackend.egg.request.PickEggsRequest;

import java.time.LocalDate;
import java.util.List;

// Interface for Egg service
public interface IEggService {
    EggDto registerEgg(Long robotId, RegisterEggRequest request);
    List<EggDto> getEggsByFarm(Long farmId, Boolean picked, LocalDate date);
    void markEggsAsPicked(Long farmId, PickEggsRequest pickEggsRequest);
}