package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.PatientLoginRequest;
import com.healthfirst.provider.dto.PatientLoginResponse;
import com.healthfirst.provider.service.PatientAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
public class PatientAuthController {
    
    private final PatientAuthService patientAuthService;
    
    @PostMapping("/login")
    public ResponseEntity<PatientLoginResponse> login(@Valid @RequestBody PatientLoginRequest request) {
        try {
            PatientLoginResponse response = patientAuthService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            PatientLoginResponse errorResponse = new PatientLoginResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setData(null);
            errorResponse.setErrorCode("INVALID_CREDENTIALS");
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            PatientLoginResponse errorResponse = new PatientLoginResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Internal server error");
            errorResponse.setData(null);
            errorResponse.setErrorCode("INTERNAL_ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
} 