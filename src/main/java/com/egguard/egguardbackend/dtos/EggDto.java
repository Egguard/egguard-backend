package com.egguard.egguardbackend.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EggDto {
    private Long id;
    private Long farmId;
    private Double coordX;
    private Double coordY;
    private Boolean broken;
    private Boolean picked;
    private LocalDateTime timestamp;
}
