package com.healthfirst.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderLoginResponse {
    private boolean success;
    private String message;
    private DataDTO data;
    private String errorCode;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataDTO {
        private String accessToken;
        private long expiresIn;
        private String tokenType;
        private ProviderDTO provider;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderDTO {
        private String providerId;
        private String email;
        private String role;
        private String specialization;
        private String verificationStatus;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private boolean isActive;
    }
} 