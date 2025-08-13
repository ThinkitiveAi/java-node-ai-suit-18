package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.PatientLoginRequest;
import com.healthfirst.provider.dto.PatientLoginResponse;
import com.healthfirst.provider.entity.Patient;
import com.healthfirst.provider.repository.PatientRepository;
import com.healthfirst.provider.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PatientAuthService {
    
    private final PatientRepository patientRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public PatientLoginResponse login(PatientLoginRequest request) {
        // Find patient by email
        Patient patient = patientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), patient.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        // Check if account is active
        if (!patient.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }
        
        // Check if email is verified (optional for login, but good practice)
        if (!patient.isEmailVerified()) {
            throw new IllegalArgumentException("Email not verified");
        }
        
        // Generate JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("patient_id", patient.getId().toString());
        claims.put("email", patient.getEmail());
        claims.put("role", "PATIENT");
        
        String token = jwtUtil.generateToken(claims, 1800); // 30 minutes expiry
        
        // Create response
        PatientLoginResponse.PatientDTO patientDTO = new PatientLoginResponse.PatientDTO();
        patientDTO.setPatientId(patient.getId());
        patientDTO.setEmail(patient.getEmail());
        patientDTO.setRole("PATIENT");
        patientDTO.setFirstName(patient.getFirstName());
        patientDTO.setLastName(patient.getLastName());
        patientDTO.setPhoneNumber(patient.getPhoneNumber());
        patientDTO.setEmailVerified(patient.isEmailVerified());
        patientDTO.setPhoneVerified(patient.isPhoneVerified());
        patientDTO.setActive(patient.isActive());
        
        PatientLoginResponse.DataDTO data = new PatientLoginResponse.DataDTO();
        data.setAccessToken(token);
        data.setExpiresIn(1800); // 30 minutes
        data.setTokenType("Bearer");
        data.setPatient(patientDTO);
        
        return new PatientLoginResponse(true, "Login successful", data, null);
    }
} 