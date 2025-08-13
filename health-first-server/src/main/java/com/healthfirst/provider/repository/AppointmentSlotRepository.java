package com.healthfirst.provider.repository;

import com.healthfirst.provider.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {
    
    List<AppointmentSlot> findByProviderIdAndSlotStartTimeBetweenOrderBySlotStartTimeAsc(
            UUID providerId, ZonedDateTime startTime, ZonedDateTime endTime);
    
    List<AppointmentSlot> findByProviderIdAndStatus(UUID providerId, AppointmentSlot.SlotStatus status);
    
    Optional<AppointmentSlot> findByBookingReference(String bookingReference);
    
    @Query("SELECT as FROM AppointmentSlot as WHERE (:providerId IS NULL OR as.provider.id = :providerId) " +
           "AND as.slotStartTime >= :startTime " +
           "AND as.slotStartTime < :endTime " +
           "AND as.status = 'AVAILABLE'")
    List<AppointmentSlot> findAvailableSlotsInTimeRange(
            @Param("providerId") UUID providerId,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime);
    
    @Query("SELECT COUNT(as) FROM AppointmentSlot as WHERE as.provider.id = :providerId " +
           "AND as.slotStartTime >= :startTime " +
           "AND as.slotStartTime < :endTime " +
           "AND as.status = 'BOOKED'")
    long countBookedSlotsInTimeRange(
            @Param("providerId") UUID providerId,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime);
    
    boolean existsByProviderIdAndSlotStartTimeAndSlotEndTimeAndStatusNot(
            UUID providerId, ZonedDateTime startTime, ZonedDateTime endTime, 
            AppointmentSlot.SlotStatus status);
} 