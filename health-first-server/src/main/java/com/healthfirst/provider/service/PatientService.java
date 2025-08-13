package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.PatientRegistrationRequest;
import com.healthfirst.provider.dto.PatientResponse;
import com.healthfirst.provider.entity.*;
import com.healthfirst.provider.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    
    private final PatientRepository patientRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public PatientResponse registerPatient(PatientRegistrationRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }
        
        // Validate age (must be at least 13 years old for COPPA compliance)
        LocalDate today = LocalDate.now();
        Period age = Period.between(request.getDateOfBirth(), today);
        if (age.getYears() < 13) {
            throw new IllegalArgumentException("Patient must be at least 13 years old");
        }
        
        // Check for existing email
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        
        // Check for existing phone number
        if (patientRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number is already registered");
        }
        
        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        // Create patient entity
        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setPasswordHash(hashedPassword);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        
        // Set address
        PatientAddress address = new PatientAddress();
        address.setStreet(request.getAddress().getStreet());
        address.setCity(request.getAddress().getCity());
        address.setState(request.getAddress().getState());
        address.setZip(request.getAddress().getZip());
        patient.setAddress(address);
        
        // Set emergency contact if provided
        if (request.getEmergencyContact() != null) {
            EmergencyContact emergencyContact = new EmergencyContact();
            emergencyContact.setName(request.getEmergencyContact().getName());
            emergencyContact.setPhone(request.getEmergencyContact().getPhone());
            emergencyContact.setRelationship(request.getEmergencyContact().getRelationship());
            patient.setEmergencyContact(emergencyContact);
        }
        
        // Set medical history if provided
        if (request.getMedicalHistory() != null && !request.getMedicalHistory().isEmpty()) {
            patient.setMedicalHistory(request.getMedicalHistory());
        }
        
        // Set insurance info if provided
        if (request.getInsuranceInfo() != null) {
            InsuranceInfo insuranceInfo = new InsuranceInfo();
            insuranceInfo.setProvider(request.getInsuranceInfo().getProvider());
            insuranceInfo.setPolicyNumber(request.getInsuranceInfo().getPolicyNumber());
            patient.setInsuranceInfo(insuranceInfo);
        }
        
        // Save patient
        Patient savedPatient = patientRepository.save(patient);
        
        // Create response
        PatientResponse.DataDTO data = new PatientResponse.DataDTO();
        data.setPatientId(savedPatient.getId());
        data.setEmail(savedPatient.getEmail());
        data.setPhoneNumber(savedPatient.getPhoneNumber());
        data.setEmailVerified(savedPatient.isEmailVerified());
        data.setPhoneVerified(savedPatient.isPhoneVerified());
        
        return new PatientResponse(true, "Patient registered successfully. Verification email sent.", data, null);
    }
} 