package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.ProviderLoginRequest;
import com.healthfirst.provider.dto.ProviderLoginResponse;
import com.healthfirst.provider.service.ProviderAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/provider")
@RequiredArgsConstructor
public class ProviderAuthController {
    private final ProviderAuthService providerAuthService;

    @PostMapping("/login")
    public ResponseEntity<ProviderLoginResponse> login(@Valid @RequestBody ProviderLoginRequest request) {
        ProviderLoginResponse response = providerAuthService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
} 