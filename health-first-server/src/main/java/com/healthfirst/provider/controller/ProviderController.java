package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;
import com.healthfirst.provider.dto.ProviderResponse;
import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.repository.ProviderRepository;
import com.healthfirst.provider.service.ProviderService;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/provider")
@RequiredArgsConstructor
public class ProviderController {
    private final ProviderService providerService;
    private final ProviderRepository providerRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerProvider(@RequestBody ProviderRegistrationRequest request) {
        try {
            ProviderResponse response = providerService.registerProvider(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ConstraintViolationException ex) {
            Map<String, String> errors = new HashMap<>();
            ex.getConstraintViolations().forEach(v -> {
                String field = v.getPropertyPath().toString();
                errors.put(field, v.getMessage());
            });
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                    "success", false,
                    "message", "Validation failed",
                    "errors", errors
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Internal server error"
            ));
        }
    }

    @PostMapping("/verify-test")
    public ResponseEntity<Map<String, Object>> verifyProviderForTest(@RequestParam String email) {
        try {
            Provider provider = providerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Provider not found"));
            
            provider.setVerificationStatus(Provider.VerificationStatus.VERIFIED);
            providerRepository.save(provider);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Provider verified successfully");
            response.put("email", email);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list-all")
    public ResponseEntity<Map<String, Object>> listAllProviders() {
        try {
            List<Provider> providers = providerRepository.findAll();
            List<Map<String, Object>> providerList = providers.stream()
                    .map(p -> {
                        Map<String, Object> providerMap = new HashMap<>();
                        providerMap.put("id", p.getId());
                        providerMap.put("email", p.getEmail());
                        providerMap.put("firstName", p.getFirstName());
                        providerMap.put("lastName", p.getLastName());
                        providerMap.put("verificationStatus", p.getVerificationStatus());
                        providerMap.put("isActive", p.isActive());
                        return providerMap;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", providers.size());
            response.put("providers", providerList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "success", false,
                "message", "Validation failed",
                "errors", errors
        ));
    }
} 