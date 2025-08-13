package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.PatientRegistrationRequest;
import com.healthfirst.provider.dto.PatientResponse;
import com.healthfirst.provider.entity.Patient;
import com.healthfirst.provider.repository.PatientRepository;
import com.healthfirst.provider.service.PatientService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
public class PatientController {
    
    private final PatientService patientService;
    private final PatientRepository patientRepository;
    
    @PostMapping("/register")
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody PatientRegistrationRequest request) {
        try {
            PatientResponse response = patientService.registerPatient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            PatientResponse errorResponse = new PatientResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setData(null);
            errorResponse.setErrorCode("VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            PatientResponse errorResponse = new PatientResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Internal server error");
            errorResponse.setData(null);
            errorResponse.setErrorCode("INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/verify-test")
    public ResponseEntity<Map<String, Object>> verifyPatient(@RequestParam String email) {
        try {
            // Find the patient and update their email verification status
            Patient patient = patientRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found with email: " + email));
            
            boolean beforeVerification = patient.isEmailVerified();
            
            patient.setEmailVerified(true);
            Patient savedPatient = patientRepository.save(patient);
            
            boolean afterVerification = savedPatient.isEmailVerified();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patient verified successfully");
            response.put("email", email);
            response.put("beforeVerification", beforeVerification);
            response.put("afterVerification", afterVerification);
            response.put("emailVerified", savedPatient.isEmailVerified());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/list-all")
    public ResponseEntity<Map<String, Object>> listAllPatients() {
        try {
            // This is a test endpoint to list all patients for debugging
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Patients retrieved successfully");
            response.put("count", 0); // Will be implemented when needed
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PatientResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        PatientResponse errorResponse = new PatientResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("Validation failed");
        errorResponse.setData(null);
        errorResponse.setErrorCode("VALIDATION_ERROR");
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<PatientResponse> handleConstraintViolation(ConstraintViolationException ex) {
        PatientResponse errorResponse = new PatientResponse();
        errorResponse.setSuccess(false);
        errorResponse.setMessage("Validation failed: " + ex.getMessage());
        errorResponse.setData(null);
        errorResponse.setErrorCode("VALIDATION_ERROR");
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
} 