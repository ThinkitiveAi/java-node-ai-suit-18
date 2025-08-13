package com.healthfirst.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {
    private boolean success;
    private String message;
    private DataDTO data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataDTO {
        private String providerId;
        private String email;
        private String verificationStatus;
    }
} 