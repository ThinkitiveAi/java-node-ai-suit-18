package com.healthfirst.provider.dto;

import com.healthfirst.provider.entity.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatientRegistrationRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in valid format")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in international format")
    private String phoneNumber;
    
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character"
    )
    private String password;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotNull(message = "Gender is required")
    private Gender gender;
    
    @Valid
    @NotNull(message = "Address is required")
    private PatientAddressDTO address;
    
    @Valid
    private EmergencyContactDTO emergencyContact;
    
    private List<String> medicalHistory;
    
    @Valid
    private InsuranceInfoDTO insuranceInfo;
    
    @Data
    public static class PatientAddressDTO {
        @NotBlank(message = "Street is required")
        @Size(max = 200, message = "Street must not exceed 200 characters")
        private String street;
        
        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;
        
        @NotBlank(message = "State is required")
        @Size(max = 50, message = "State must not exceed 50 characters")
        private String state;
        
        @NotBlank(message = "ZIP code is required")
        @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "ZIP code must be in valid format")
        private String zip;
    }
    
    @Data
    public static class EmergencyContactDTO {
        @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
        private String name;
        
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Emergency contact phone must be in international format")
        private String phone;
        
        @Size(max = 50, message = "Relationship must not exceed 50 characters")
        private String relationship;
    }
    
    @Data
    public static class InsuranceInfoDTO {
        @Size(max = 100, message = "Insurance provider must not exceed 100 characters")
        private String provider;
        
        @Size(max = 50, message = "Policy number must not exceed 50 characters")
        private String policyNumber;
    }
} 