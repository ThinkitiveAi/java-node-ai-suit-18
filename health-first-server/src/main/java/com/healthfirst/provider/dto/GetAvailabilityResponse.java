package com.healthfirst.provider.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAvailabilityResponse {
    
    private Boolean success;
    private AvailabilityData data;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailabilityData {
        private UUID providerId;
        private AvailabilitySummary availabilitySummary;
        private List<DayAvailability> availability;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailabilitySummary {
        private Integer totalSlots;
        private Integer availableSlots;
        private Integer bookedSlots;
        private Integer cancelledSlots;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayAvailability {
        private String date;
        private List<SlotInfo> slots;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SlotInfo {
        private UUID slotId;
        private String startTime;
        private String endTime;
        private String status;
        private String appointmentType;
        private LocationInfo location;
        private PricingInfo pricing;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationInfo {
        private String type;
        private String address;
        private String roomNumber;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PricingInfo {
        private BigDecimal baseFee;
        private Boolean insuranceAccepted;
    }
} 