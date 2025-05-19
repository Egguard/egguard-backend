package com.egguard.egguardbackend.farm;

import java.time.LocalDate;

public interface IFarmStatsService {
    public FarmStatsDto getFarmStats(Long farmId, LocalDate from, LocalDate to);
}
