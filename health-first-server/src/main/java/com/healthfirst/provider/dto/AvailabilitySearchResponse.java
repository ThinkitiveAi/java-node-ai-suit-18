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
public class AvailabilitySearchResponse {
    
    private Boolean success;
    private SearchData data;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchData {
        private SearchCriteria searchCriteria;
        private Integer totalResults;
        private List<SearchResult> results;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchCriteria {
        private String date;
        private String specialization;
        private String location;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchResult {
        private ProviderInfo provider;
        private List<AvailableSlot> availableSlots;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProviderInfo {
        private UUID id;
        private String name;
        private String specialization;
        private Integer yearsOfExperience;
        private Double rating;
        private String clinicAddress;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailableSlot {
        private UUID slotId;
        private String date;
        private String startTime;
        private String endTime;
        private String appointmentType;
        private LocationInfo location;
        private PricingInfo pricing;
        private List<String> specialRequirements;
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
        private String currency;
    }
} 