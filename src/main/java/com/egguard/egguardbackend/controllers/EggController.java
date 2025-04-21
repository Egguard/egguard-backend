package com.egguard.egguardbackend.controllers;

import com.egguard.egguardbackend.dtos.EggDto;
import com.egguard.egguardbackend.requests.RegisterEggRequest;
import com.egguard.egguardbackend.requests.PickEggsRequest;
import com.egguard.egguardbackend.services.IEggService;
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
        // TODO: Add error handling for Robot not found (404)

        EggDto registeredEgg = eggService.registerEgg(robotId, request);
        if (registeredEgg != null) { // Simplified check
             return ResponseEntity.status(HttpStatus.CREATED).body(registeredEgg);
        } else {
             // How duplicates are handled determines the response here (e.g., 200 OK with existing egg?)
             return ResponseEntity.ok().build(); // Placeholder for 200 OK
        }
    }

    @GetMapping("/farms/{farm_id}/eggs")
    public ResponseEntity<List<EggDto>> getEggsByFarm(
            @PathVariable("farm_id") Long farmId,
            @RequestParam(required = false) Boolean picked,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // TODO: Add error handling for Farm not found (404)
        List<EggDto> eggs = eggService.getEggsByFarm(farmId, picked, date);
        return ResponseEntity.ok(eggs);
    }

    @PatchMapping("/farms/{farm_id}/eggs/picked")
    public ResponseEntity<Void> markEggsAsPicked(
            @PathVariable("farm_id") Long farmId,
            @RequestBody PickEggsRequest request) {
        // TODO: Add error handling for Farm not found (404)
        eggService.markEggsAsPicked(farmId, request);
        return ResponseEntity.ok().build();
    }
}
