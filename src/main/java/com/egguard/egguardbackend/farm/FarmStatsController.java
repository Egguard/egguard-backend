package com.egguard.egguardbackend.farm;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
public class FarmStatsController {

    private final IFarmStatsService farmStatsService;

    @GetMapping("/{farm_id}/stats")
    public ResponseEntity<FarmStatsDto> getFarmStats(
            @PathVariable("farm_id") Long farmId,
            @RequestParam(required = false, value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false, value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate today = LocalDate.now();
        if (endDate == null) endDate = today;
        if (startDate == null) startDate = endDate.minusDays(7);

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Start date cannot be after end date.");
        }
        FarmStatsDto stats = farmStatsService.getFarmStats(farmId, startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}
