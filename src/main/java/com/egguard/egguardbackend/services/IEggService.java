package com.egguard.egguardbackend.services;

import com.egguard.egguardbackend.dtos.EggDto;
import com.egguard.egguardbackend.requests.RegisterEggRequest;
import com.egguard.egguardbackend.requests.PickEggsRequest;

import java.time.LocalDate;
import java.util.List;

// Interface for Egg service
public interface IEggService {
    EggDto registerEgg(Long robotId, RegisterEggRequest request);
    List<EggDto> getEggsByFarm(Long farmId, Boolean picked, LocalDate date);
    void markEggsAsPicked(Long farmId, PickEggsRequest pickEggsRequest);
}