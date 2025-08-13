package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;
import com.healthfirst.provider.dto.ProviderResponse;
import com.healthfirst.provider.entity.ClinicAddress;
import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.repository.ProviderRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;
    private final Validator validator;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private static final Set<String> ALLOWED_SPECIALIZATIONS = Set.of(
            "Cardiology", "Dermatology", "Neurology", "Pediatrics", "Oncology", "Orthopedics", "General Medicine"
    );

    @Transactional
    public ProviderResponse registerProvider(ProviderRegistrationRequest request) {
        // Validate bean constraints
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // Custom validations
        Set<String> errors = new HashSet<>();
        if (providerRepository.findByEmail(request.getEmail()).isPresent()) {
            errors.add("Email already exists");
        }
        if (providerRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            errors.add("Phone number already exists");
        }
        if (providerRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            errors.add("License number already exists");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            errors.add("Passwords do not match");
        }
        if (!isValidPassword(request.getPassword())) {
            errors.add("Password must be at least 8 characters, contain uppercase, lowercase, number, and special character");
        }
        if (!ALLOWED_SPECIALIZATIONS.contains(request.getSpecialization())) {
            errors.add("Specialization is not valid");
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }

        // Hash password
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Map DTO to entity
        Provider provider = new Provider();
        provider.setFirstName(request.getFirstName());
        provider.setLastName(request.getLastName());
        provider.setEmail(request.getEmail());
        provider.setPhoneNumber(request.getPhoneNumber());
        provider.setPasswordHash(passwordHash);
        provider.setSpecialization(request.getSpecialization());
        provider.setLicenseNumber(request.getLicenseNumber());
        provider.setYearsOfExperience(request.getYearsOfExperience());
        provider.setClinicAddress(new ClinicAddress(
                request.getClinicAddress().getStreet(),
                request.getClinicAddress().getCity(),
                request.getClinicAddress().getState(),
                request.getClinicAddress().getZip()
        ));
        provider.setVerificationStatus(Provider.VerificationStatus.PENDING);
        provider.setIsActive(true);
        // createdAt and updatedAt are set by default

        Provider saved = providerRepository.save(provider);

        // TODO: Send verification email

        return new ProviderResponse(
                true,
                "Provider registered successfully. Verification email sent.",
                new ProviderResponse.DataDTO(
                        saved.getId().toString(),
                        saved.getEmail(),
                        saved.getVerificationStatus().name().toLowerCase()
                )
        );
    }

    private boolean isValidPassword(String password) {
        // At least 8 chars, 1 upper, 1 lower, 1 digit, 1 special
        return password != null && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
} 