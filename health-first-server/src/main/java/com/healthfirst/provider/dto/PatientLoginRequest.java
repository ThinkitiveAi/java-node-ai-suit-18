package com.healthfirst.provider.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientLoginRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in valid format")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
} 