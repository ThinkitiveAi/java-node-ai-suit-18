package com.healthfirst.provider.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "provider_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "timezone", nullable = false)
    private String timezone;
    
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern")
    private RecurrencePattern recurrencePattern;
    
    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;
    
    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration = 30; // minutes
    
    @Column(name = "break_duration", nullable = false)
    private Integer breakDuration = 0; // minutes
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AvailabilityStatus status = AvailabilityStatus.AVAILABLE;
    
    @Column(name = "max_appointments_per_slot", nullable = false)
    private Integer maxAppointmentsPerSlot = 1;
    
    @Column(name = "current_appointments", nullable = false)
    private Integer currentAppointments = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;
    
    @Embedded
    private AvailabilityLocation location;
    
    @Embedded
    private AvailabilityPricing pricing;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @ElementCollection
    @CollectionTable(name = "availability_special_requirements", 
                     joinColumns = @JoinColumn(name = "availability_id"))
    @Column(name = "requirement")
    private List<String> specialRequirements;
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
    
    public enum RecurrencePattern {
        DAILY, WEEKLY, MONTHLY
    }
    
    public enum AvailabilityStatus {
        AVAILABLE, BOOKED, CANCELLED, BLOCKED, MAINTENANCE
    }
    
    public enum AppointmentType {
        CONSULTATION, FOLLOW_UP, EMERGENCY, TELEMEDICINE
    }
} 