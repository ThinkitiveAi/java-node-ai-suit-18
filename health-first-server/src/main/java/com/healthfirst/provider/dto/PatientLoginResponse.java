package com.healthfirst.provider.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientLoginResponse {
    private boolean success;
    private String message;
    private DataDTO data;
    private String errorCode;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataDTO {
        private String accessToken;
        private int expiresIn;
        private String tokenType;
        private PatientDTO patient;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientDTO {
        private UUID patientId;
        private String email;
        private String role;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private boolean emailVerified;
        private boolean phoneVerified;
        private boolean active;
    }
} 