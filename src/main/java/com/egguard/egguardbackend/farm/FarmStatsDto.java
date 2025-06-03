package com.egguard.egguardbackend.farm;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmStatsDto {
    private Long totalPickedEggs;
    private Long averageNotBrokenEggsPickedPerDay;
    private Long averageBrokenEggsPickedPerDay;
    private Double brokenEggsPercentage;
}
