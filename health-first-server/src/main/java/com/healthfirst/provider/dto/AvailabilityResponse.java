package com.healthfirst.provider.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityResponse {
    
    private Boolean success;
    private String message;
    private AvailabilityData data;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailabilityData {
        private UUID availabilityId;
        private Integer slotsCreated;
        private DateRange dateRange;
        private Integer totalAppointmentsAvailable;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateRange {
        private LocalDate start;
        private LocalDate end;
    }
} 