package com.healthfirst.provider.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id", nullable = false)
    private ProviderAvailability availability;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;
    
    @Column(name = "slot_start_time", nullable = false)
    private ZonedDateTime slotStartTime;
    
    @Column(name = "slot_end_time", nullable = false)
    private ZonedDateTime slotEndTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @Column(name = "appointment_type", nullable = false)
    private String appointmentType;
    
    @Column(name = "booking_reference", unique = true)
    private String bookingReference;
    
    public enum SlotStatus {
        AVAILABLE, BOOKED, CANCELLED, BLOCKED
    }
} 