package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.CreateAvailabilityRequest;
import com.healthfirst.provider.dto.AvailabilityResponse;
import com.healthfirst.provider.dto.GetAvailabilityResponse;
import com.healthfirst.provider.service.ProviderAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/provider")
@RequiredArgsConstructor
@Slf4j
public class ProviderAvailabilityController {
    
    private final ProviderAvailabilityService availabilityService;
    
    @PostMapping("/availability")
    public ResponseEntity<AvailabilityResponse> createAvailability(
            @RequestParam UUID providerId,
            @Valid @RequestBody CreateAvailabilityRequest request) {
        try {
            AvailabilityResponse response = availabilityService.createAvailability(providerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Error creating availability: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AvailabilityResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Unexpected error creating availability", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AvailabilityResponse.builder()
                    .success(false)
                    .message("An unexpected error occurred")
                    .build());
        }
    }
    
    @GetMapping("/{providerId}/availability")
    public ResponseEntity<GetAvailabilityResponse> getProviderAvailability(
            @PathVariable UUID providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String appointmentType) {
        try {
            GetAvailabilityResponse response = availabilityService.getProviderAvailability(
                    providerId, startDate, endDate, status, appointmentType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error getting provider availability: {}", e.getMessage());
            return ResponseEntity.badRequest().body(GetAvailabilityResponse.builder()
                    .success(false)
                    .build());
        } catch (Exception e) {
            log.error("Unexpected error getting provider availability", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GetAvailabilityResponse.builder()
                    .success(false)
                    .build());
        }
    }
    
    @PutMapping("/availability/{slotId}")
    public ResponseEntity<Map<String, Object>> updateAvailabilitySlot(
            @PathVariable UUID slotId,
            @RequestBody Map<String, Object> updates) {
        try {
            availabilityService.updateAvailabilitySlot(slotId, updates);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Availability slot updated successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error updating availability slot: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error updating availability slot", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred"
            ));
        }
    }
    
    @DeleteMapping("/availability/{slotId}")
    public ResponseEntity<Map<String, Object>> deleteAvailabilitySlot(
            @PathVariable UUID slotId,
            @RequestParam(required = false) Boolean deleteRecurring,
            @RequestParam(required = false) String reason) {
        try {
            availabilityService.deleteAvailabilitySlot(slotId, deleteRecurring, reason);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Availability slot deleted successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting availability slot: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Unexpected error deleting availability slot", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred"
            ));
        }
    }
} 