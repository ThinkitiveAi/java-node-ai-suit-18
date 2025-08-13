package com.healthfirst.provider.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private boolean success;
    private String message;
    private DataDTO data;
    private String errorCode;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataDTO {
        private UUID patientId;
        private String email;
        private String phoneNumber;
        private boolean emailVerified;
        private boolean phoneVerified;
    }
} 