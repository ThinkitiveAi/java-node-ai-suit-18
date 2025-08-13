package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.AvailabilitySearchRequest;
import com.healthfirst.provider.dto.AvailabilitySearchResponse;
import com.healthfirst.provider.service.AvailabilitySearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Slf4j
public class AvailabilitySearchController {
    
    private final AvailabilitySearchService searchService;
    
    @GetMapping("/search")
    public ResponseEntity<AvailabilitySearchResponse> searchAvailability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String appointmentType,
            @RequestParam(required = false) Boolean insuranceAccepted,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String timezone,
            @RequestParam(defaultValue = "true") Boolean availableOnly) {
        
        try {
            AvailabilitySearchRequest request = new AvailabilitySearchRequest();
            request.setDate(date);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setSpecialization(specialization);
            request.setLocation(location);
            request.setAppointmentType(appointmentType);
            request.setInsuranceAccepted(insuranceAccepted);
            request.setMaxPrice(maxPrice);
            request.setTimezone(timezone);
            request.setAvailableOnly(availableOnly);
            
            AvailabilitySearchResponse response = searchService.searchAvailability(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching availability", e);
            return ResponseEntity.status(500).body(AvailabilitySearchResponse.builder()
                    .success(false)
                    .build());
        }
    }
} 