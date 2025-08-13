package com.healthfirst.provider.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceInfo {
    
    @Column(name = "provider", length = 100)
    private String provider;
    
    @Column(name = "policy_number", length = 50)
    private String policyNumber;
} 