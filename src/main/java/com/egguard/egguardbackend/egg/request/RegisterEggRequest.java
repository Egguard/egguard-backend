package com.egguard.egguardbackend.egg.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterEggRequest {

    @NotNull(message = "Coordinate X must be provided")
    private Double coordX;

    @NotNull(message = "Coordinate Y must be provided")
    private Double coordY;

    @NotNull(message = "Broken status must be provided")
    private Boolean broken;
} 