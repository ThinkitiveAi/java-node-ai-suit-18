package com.healthfirst.provider.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityPricing {
    
    @jakarta.persistence.Column(name = "base_fee", precision = 10, scale = 2)
    private BigDecimal baseFee;
    
    @jakarta.persistence.Column(name = "insurance_accepted")
    private Boolean insuranceAccepted = false;
    
    @jakarta.persistence.Column(name = "currency", length = 3)
    private String currency = "USD";
} 