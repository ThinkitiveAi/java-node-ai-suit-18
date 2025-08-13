package com.healthfirst.provider.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityLocation {
    
    @Enumerated(EnumType.STRING)
    @jakarta.persistence.Column(name = "location_type")
    private LocationType type;
    
    @jakarta.persistence.Column(name = "location_address")
    private String address;
    
    @jakarta.persistence.Column(name = "room_number")
    private String roomNumber;
    
    public enum LocationType {
        CLINIC, HOSPITAL, TELEMEDICINE, HOME_VISIT
    }
} 