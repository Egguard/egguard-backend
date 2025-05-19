package com.egguard.egguardbackend.egg;

import com.egguard.egguardbackend.egg.request.RegisterEggRequest;
import com.egguard.egguardbackend.egg.request.PickEggsRequest;
import com.egguard.egguardbackend.egg.exception.DuplicateEggException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EggController {

    private final IEggService eggService;

    @PostMapping("/robots/{robot_id}/eggs")
    public ResponseEntity<EggDto> registerEgg(
            @PathVariable("robot_id") Long robotId,
            @Valid @RequestBody RegisterEggRequest request) {
        try {
            EggDto registeredEgg = eggService.registerEgg(robotId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredEgg);
        } catch (DuplicateEggException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/farms/{farm_id}/eggs")
    public ResponseEntity<List<EggDto>> getEggsByFarm(
            @PathVariable("farm_id") Long farmId,
            @RequestParam(required = false) Boolean picked,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<EggDto> eggs = eggService.getEggsByFarm(farmId, picked, date);
        return ResponseEntity.ok(eggs);
    }

    @PatchMapping("/farms/{farm_id}/eggs/picked")
    public ResponseEntity<Void> markEggsAsPicked(
            @PathVariable("farm_id") Long farmId,
            @RequestBody PickEggsRequest request) {
        eggService.markEggsAsPicked(farmId, request);
        return ResponseEntity.ok().build();
    }
/*
    @GetMapping("/farms/{farm_id}/eggs/stats-summary")
    public ResponseEntity<EggsStatsSummaryDto> getEggsStatsSummary(
            @PathVariable("farm_id") Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        EggsStatsSummaryDto eggsStatsSummaryDto = eggService.getEggsStatsSummary(farmId, from, to);
        return ResponseEntity.ok(eggsStatsSummaryDto);
    }
*/
}
