package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.ProviderLoginRequest;
import com.healthfirst.provider.dto.ProviderLoginResponse;
import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.repository.ProviderRepository;
import com.healthfirst.provider.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProviderAuthService {
    private final ProviderRepository providerRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ProviderLoginResponse login(ProviderLoginRequest request) {
        // Find provider by email
        Provider provider = providerRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (provider == null) {
            return createErrorResponse("Invalid credentials", "INVALID_CREDENTIALS");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), provider.getPasswordHash())) {
            return createErrorResponse("Invalid credentials", "INVALID_CREDENTIALS");
        }

        // Check if account is active
        if (!provider.isActive()) {
            return createErrorResponse("Account is deactivated", "ACCOUNT_DEACTIVATED");
        }

        // Check if account is verified
        if (!Provider.VerificationStatus.VERIFIED.equals(provider.getVerificationStatus())) {
            return createErrorResponse("Account not verified", "ACCOUNT_NOT_VERIFIED");
        }
        

        // Generate JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("provider_id", provider.getId().toString());
        claims.put("email", provider.getEmail());
        claims.put("role", "PROVIDER");
        claims.put("specialization", provider.getSpecialization());
        claims.put("verification_status", provider.getVerificationStatus());

        String token = JwtUtil.generateToken(claims, provider.getEmail());

        // Create success response
        ProviderLoginResponse.ProviderDTO providerDTO = new ProviderLoginResponse.ProviderDTO(
                provider.getId().toString(),
                provider.getEmail(),
                "PROVIDER",
                provider.getSpecialization(),
                provider.getVerificationStatus().name(),
                provider.getFirstName(),
                provider.getLastName(),
                provider.getPhoneNumber(),
                provider.isActive()
        );

        ProviderLoginResponse.DataDTO dataDTO = new ProviderLoginResponse.DataDTO(
                token,
                3600L, // 1 hour in seconds
                "Bearer",
                providerDTO
        );

        return new ProviderLoginResponse(true, "Login successful", dataDTO, null);
    }

    private ProviderLoginResponse createErrorResponse(String message, String errorCode) {
        return new ProviderLoginResponse(false, message, null, errorCode);
    }
} 