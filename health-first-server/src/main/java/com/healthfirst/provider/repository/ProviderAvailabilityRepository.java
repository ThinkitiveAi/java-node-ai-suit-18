package com.healthfirst.provider.repository;

import com.healthfirst.provider.entity.ProviderAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, UUID> {
    
    List<ProviderAvailability> findByProviderIdAndDateBetweenOrderByDateAscStartTimeAsc(
            UUID providerId, LocalDate startDate, LocalDate endDate);
    
    List<ProviderAvailability> findByProviderIdAndDateAndStatus(
            UUID providerId, LocalDate date, ProviderAvailability.AvailabilityStatus status);
    
    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.date BETWEEN :startDate AND :endDate " +
           "AND (:status IS NULL OR pa.status = :status) " +
           "AND (:appointmentType IS NULL OR pa.appointmentType = :appointmentType)")
    List<ProviderAvailability> findAvailabilityWithFilters(
            @Param("providerId") UUID providerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ProviderAvailability.AvailabilityStatus status,
            @Param("appointmentType") ProviderAvailability.AppointmentType appointmentType);
    
    @Query("SELECT COUNT(pa) FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.date = :date " +
           "AND pa.startTime < :endTime " +
           "AND pa.endTime > :startTime " +
           "AND pa.status != 'CANCELLED'")
    long countOverlappingSlots(
            @Param("providerId") UUID providerId,
            @Param("date") LocalDate date,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);
    
    @Query("SELECT COUNT(pa) > 0 FROM ProviderAvailability pa WHERE pa.provider.id = :providerId " +
           "AND pa.date = :date " +
           "AND pa.startTime = :startTime " +
           "AND pa.endTime = :endTime " +
           "AND pa.status != :status")
    boolean existsByProviderIdAndDateAndStartTimeAndEndTimeAndStatusNot(
            @Param("providerId") UUID providerId, 
            @Param("date") LocalDate date, 
            @Param("startTime") LocalTime startTime, 
            @Param("endTime") LocalTime endTime, 
            @Param("status") ProviderAvailability.AvailabilityStatus status);
} 