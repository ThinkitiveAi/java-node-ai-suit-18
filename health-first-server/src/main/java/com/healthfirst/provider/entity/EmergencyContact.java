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
public class EmergencyContact {
    
    @Column(name = "name", length = 100)
    private String name;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "relationship", length = 50)
    private String relationship;
} 