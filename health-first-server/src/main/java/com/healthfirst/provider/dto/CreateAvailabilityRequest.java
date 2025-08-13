package com.healthfirst.provider.dto;

import com.healthfirst.provider.entity.ProviderAvailability;
import com.healthfirst.provider.entity.AvailabilityLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateAvailabilityRequest {
    
    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDate date;
    
    @NotNull(message = "Start time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:mm format")
    private String startTime;
    
    @NotNull(message = "End time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:mm format")
    private String endTime;
    
    @NotBlank(message = "Timezone is required")
    @Pattern(regexp = "^[A-Za-z_]+/[A-Za-z_]+$", message = "Invalid timezone format")
    private String timezone;
    
    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Max(value = 480, message = "Slot duration cannot exceed 8 hours")
    private Integer slotDuration = 30;
    
    @Min(value = 0, message = "Break duration cannot be negative")
    @Max(value = 120, message = "Break duration cannot exceed 2 hours")
    private Integer breakDuration = 0;
    
    private Boolean isRecurring = false;
    
    private ProviderAvailability.RecurrencePattern recurrencePattern;
    
    @Future(message = "Recurrence end date must be in the future")
    private LocalDate recurrenceEndDate;
    
    @NotNull(message = "Appointment type is required")
    private ProviderAvailability.AppointmentType appointmentType = ProviderAvailability.AppointmentType.CONSULTATION;
    
    @Valid
    @NotNull(message = "Location information is required")
    private LocationDTO location;
    
    @Valid
    private PricingDTO pricing;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    @Size(max = 10, message = "Cannot have more than 10 special requirements")
    private List<String> specialRequirements;
    
    @Data
    public static class LocationDTO {
        @NotNull(message = "Location type is required")
        private AvailabilityLocation.LocationType type;
        
        @NotBlank(message = "Address is required for physical locations")
        private String address;
        
        private String roomNumber;
    }
    
    @Data
    public static class PricingDTO {
        @DecimalMin(value = "0.0", inclusive = false, message = "Base fee must be greater than 0")
        @Digits(integer = 6, fraction = 2, message = "Base fee must have at most 6 digits before decimal and 2 after")
        private BigDecimal baseFee;
        
        private Boolean insuranceAccepted = false;
        
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code")
        private String currency = "USD";
    }
} 