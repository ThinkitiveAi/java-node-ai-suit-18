package com.healthfirst.provider.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AvailabilitySearchRequest {
    
    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
    private String specialization;
    private String location;
    private String appointmentType;
    private Boolean insuranceAccepted;
    private BigDecimal maxPrice;
    private String timezone;
    private Boolean availableOnly = true;
} 