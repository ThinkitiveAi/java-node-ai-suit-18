package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.CreateAvailabilityRequest;
import com.healthfirst.provider.dto.AvailabilityResponse;
import com.healthfirst.provider.dto.GetAvailabilityResponse;
import com.healthfirst.provider.entity.*;
import com.healthfirst.provider.repository.ProviderAvailabilityRepository;
import com.healthfirst.provider.repository.AppointmentSlotRepository;
import com.healthfirst.provider.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderAvailabilityService {
    
    private final ProviderAvailabilityRepository availabilityRepository;
    private final AppointmentSlotRepository slotRepository;
    private final ProviderRepository providerRepository;
    
    @Transactional
    public AvailabilityResponse createAvailability(UUID providerId, CreateAvailabilityRequest request) {
        // Validate provider exists and is active
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));
        
        if (!provider.isActive()) {
            throw new IllegalArgumentException("Provider is not active");
        }
        
        // Validate time format and logic
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = LocalTime.parse(request.getEndTime());
        
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Check for overlapping slots
        if (hasOverlappingSlots(providerId, request.getDate(), startTime, endTime)) {
            throw new IllegalArgumentException("Time slot overlaps with existing availability");
        }
        
        // Create availability entity
        ProviderAvailability availability = ProviderAvailability.builder()
                .provider(provider)
                .date(request.getDate())
                .startTime(startTime)
                .endTime(endTime)
                .timezone(request.getTimezone())
                .slotDuration(request.getSlotDuration())
                .breakDuration(request.getBreakDuration())
                .isRecurring(request.getIsRecurring())
                .recurrencePattern(request.getRecurrencePattern())
                .recurrenceEndDate(request.getRecurrenceEndDate())
                .appointmentType(request.getAppointmentType())
                .location(mapLocation(request.getLocation()))
                .pricing(mapPricing(request.getPricing()))
                .notes(request.getNotes())
                .specialRequirements(request.getSpecialRequirements())
                .build();
        
        availability = availabilityRepository.save(availability);
        
        // Generate appointment slots
        List<AppointmentSlot> slots = generateAppointmentSlots(availability);
        slotRepository.saveAll(slots);
        
        // Calculate total appointments available
        int totalAppointments = slots.size();
        
        return AvailabilityResponse.builder()
                .success(true)
                .message("Availability slots created successfully")
                .data(AvailabilityResponse.AvailabilityData.builder()
                        .availabilityId(availability.getId())
                        .slotsCreated(slots.size())
                        .dateRange(AvailabilityResponse.DateRange.builder()
                                .start(request.getDate())
                                .end(request.getRecurrenceEndDate() != null ? request.getRecurrenceEndDate() : request.getDate())
                                .build())
                        .totalAppointmentsAvailable(totalAppointments)
                        .build())
                .build();
    }
    
    public GetAvailabilityResponse getProviderAvailability(UUID providerId, LocalDate startDate, 
                                                         LocalDate endDate, String status, String appointmentType) {
        // Validate provider exists
        if (!providerRepository.existsById(providerId)) {
            throw new IllegalArgumentException("Provider not found");
        }
        
        // Parse filters
        ProviderAvailability.AvailabilityStatus statusFilter = null;
        if (status != null) {
            try {
                statusFilter = ProviderAvailability.AvailabilityStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status filter");
            }
        }
        
        ProviderAvailability.AppointmentType appointmentTypeFilter = null;
        if (appointmentType != null) {
            try {
                appointmentTypeFilter = ProviderAvailability.AppointmentType.valueOf(appointmentType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid appointment type filter");
            }
        }
        
        // Get availability data
        List<ProviderAvailability> availabilities = availabilityRepository.findAvailabilityWithFilters(
                providerId, startDate, endDate, statusFilter, appointmentTypeFilter);
        
        // Group by date and create response
        Map<LocalDate, List<ProviderAvailability>> groupedByDate = availabilities.stream()
                .collect(Collectors.groupingBy(ProviderAvailability::getDate));
        
        List<GetAvailabilityResponse.DayAvailability> dayAvailabilities = groupedByDate.entrySet().stream()
                .map(entry -> createDayAvailability(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(GetAvailabilityResponse.DayAvailability::getDate))
                .collect(Collectors.toList());
        
        // Calculate summary
        GetAvailabilityResponse.AvailabilitySummary summary = calculateAvailabilitySummary(availabilities);
        
        return GetAvailabilityResponse.builder()
                .success(true)
                .data(GetAvailabilityResponse.AvailabilityData.builder()
                        .providerId(providerId)
                        .availabilitySummary(summary)
                        .availability(dayAvailabilities)
                        .build())
                .build();
    }
    
    @Transactional
    public void updateAvailabilitySlot(UUID slotId, Map<String, Object> updates) {
        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        
        // Apply updates
        if (updates.containsKey("startTime")) {
            ZonedDateTime newStartTime = ZonedDateTime.parse((String) updates.get("startTime"));
            slot.setSlotStartTime(newStartTime);
        }
        
        if (updates.containsKey("endTime")) {
            ZonedDateTime newEndTime = ZonedDateTime.parse((String) updates.get("endTime"));
            slot.setSlotEndTime(newEndTime);
        }
        
        if (updates.containsKey("status")) {
            AppointmentSlot.SlotStatus newStatus = AppointmentSlot.SlotStatus.valueOf(
                    ((String) updates.get("status")).toUpperCase());
            slot.setStatus(newStatus);
        }
        
        if (updates.containsKey("notes")) {
            // Update the parent availability notes
            ProviderAvailability availability = slot.getAvailability();
            availability.setNotes((String) updates.get("notes"));
            availabilityRepository.save(availability);
        }
        
        slotRepository.save(slot);
    }
    
    @Transactional
    public void deleteAvailabilitySlot(UUID slotId, Boolean deleteRecurring, String reason) {
        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        
        if (deleteRecurring != null && deleteRecurring) {
            // Delete all recurring slots
            ProviderAvailability availability = slot.getAvailability();
            if (availability.getIsRecurring()) {
                List<AppointmentSlot> recurringSlots = slotRepository.findByProviderIdAndSlotStartTimeBetweenOrderBySlotStartTimeAsc(
                        slot.getProvider().getId(), 
                        slot.getSlotStartTime().withZoneSameInstant(ZoneOffset.UTC),
                        availability.getRecurrenceEndDate().atStartOfDay(ZoneOffset.UTC));
                
                slotRepository.deleteAll(recurringSlots);
            }
        } else {
            // Delete only this slot
            slotRepository.delete(slot);
        }
    }
    
    private boolean hasOverlappingSlots(UUID providerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return availabilityRepository.countOverlappingSlots(providerId, date, startTime.toString(), endTime.toString()) > 0;
    }
    
    private AvailabilityLocation mapLocation(CreateAvailabilityRequest.LocationDTO locationDTO) {
        if (locationDTO == null) return null;
        
        return AvailabilityLocation.builder()
                .type(locationDTO.getType())
                .address(locationDTO.getAddress())
                .roomNumber(locationDTO.getRoomNumber())
                .build();
    }
    
    private AvailabilityPricing mapPricing(CreateAvailabilityRequest.PricingDTO pricingDTO) {
        if (pricingDTO == null) return null;
        
        return AvailabilityPricing.builder()
                .baseFee(pricingDTO.getBaseFee())
                .insuranceAccepted(pricingDTO.getInsuranceAccepted())
                .currency(pricingDTO.getCurrency())
                .build();
    }
    
    private List<AppointmentSlot> generateAppointmentSlots(ProviderAvailability availability) {
        List<AppointmentSlot> slots = new ArrayList<>();
        
        LocalTime currentTime = availability.getStartTime();
        LocalTime endTime = availability.getEndTime();
        
        while (currentTime.plusMinutes(availability.getSlotDuration()).isBefore(endTime) || 
               currentTime.plusMinutes(availability.getSlotDuration()).equals(endTime)) {
            
            LocalTime slotEndTime = currentTime.plusMinutes(availability.getSlotDuration());
            
            // Convert to ZonedDateTime for the specific date and timezone
            ZoneId zoneId = ZoneId.of(availability.getTimezone());
            ZonedDateTime slotStartZoned = availability.getDate().atTime(currentTime).atZone(zoneId);
            ZonedDateTime slotEndZoned = availability.getDate().atTime(slotEndTime).atZone(zoneId);
            
            AppointmentSlot slot = AppointmentSlot.builder()
                    .availability(availability)
                    .provider(availability.getProvider())
                    .slotStartTime(slotStartZoned)
                    .slotEndTime(slotEndZoned)
                    .status(AppointmentSlot.SlotStatus.AVAILABLE)
                    .appointmentType(availability.getAppointmentType().name())
                    .build();
            
            slots.add(slot);
            
            // Move to next slot (including break)
            currentTime = slotEndTime.plusMinutes(availability.getBreakDuration());
        }
        
        return slots;
    }
    
    private GetAvailabilityResponse.DayAvailability createDayAvailability(LocalDate date, List<ProviderAvailability> availabilities) {
        List<GetAvailabilityResponse.SlotInfo> slots = availabilities.stream()
                .flatMap(availability -> {
                    // Get appointment slots for this availability
                    List<AppointmentSlot> appointmentSlots = slotRepository.findByProviderIdAndSlotStartTimeBetweenOrderBySlotStartTimeAsc(
                            availability.getProvider().getId(),
                            date.atStartOfDay(ZoneOffset.UTC),
                            date.plusDays(1).atStartOfDay(ZoneOffset.UTC));
                    
                    return appointmentSlots.stream().map(this::mapToSlotInfo);
                })
                .collect(Collectors.toList());
        
        return GetAvailabilityResponse.DayAvailability.builder()
                .date(date.toString())
                .slots(slots)
                .build();
    }
    
    private GetAvailabilityResponse.SlotInfo mapToSlotInfo(AppointmentSlot slot) {
        return GetAvailabilityResponse.SlotInfo.builder()
                .slotId(slot.getId())
                .startTime(slot.getSlotStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .endTime(slot.getSlotEndTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .status(slot.getStatus().name())
                .appointmentType(slot.getAppointmentType())
                .location(mapToLocationInfo(slot.getAvailability().getLocation()))
                .pricing(mapToPricingInfo(slot.getAvailability().getPricing()))
                .build();
    }
    
    private GetAvailabilityResponse.LocationInfo mapToLocationInfo(AvailabilityLocation location) {
        if (location == null) return null;
        
        return GetAvailabilityResponse.LocationInfo.builder()
                .type(location.getType().name())
                .address(location.getAddress())
                .roomNumber(location.getRoomNumber())
                .build();
    }
    
    private GetAvailabilityResponse.PricingInfo mapToPricingInfo(AvailabilityPricing pricing) {
        if (pricing == null) return null;
        
        return GetAvailabilityResponse.PricingInfo.builder()
                .baseFee(pricing.getBaseFee())
                .insuranceAccepted(pricing.getInsuranceAccepted())
                .build();
    }
    
    private GetAvailabilityResponse.AvailabilitySummary calculateAvailabilitySummary(List<ProviderAvailability> availabilities) {
        int totalSlots = 0;
        int availableSlots = 0;
        int bookedSlots = 0;
        int cancelledSlots = 0;
        
        for (ProviderAvailability availability : availabilities) {
            List<AppointmentSlot> slots = slotRepository.findByProviderIdAndSlotStartTimeBetweenOrderBySlotStartTimeAsc(
                    availability.getProvider().getId(),
                    availability.getDate().atStartOfDay(ZoneOffset.UTC),
                    availability.getDate().plusDays(1).atStartOfDay(ZoneOffset.UTC));
            
            totalSlots += slots.size();
            
            for (AppointmentSlot slot : slots) {
                switch (slot.getStatus()) {
                    case AVAILABLE:
                        availableSlots++;
                        break;
                    case BOOKED:
                        bookedSlots++;
                        break;
                    case CANCELLED:
                        cancelledSlots++;
                        break;
                }
            }
        }
        
        return GetAvailabilityResponse.AvailabilitySummary.builder()
                .totalSlots(totalSlots)
                .availableSlots(availableSlots)
                .bookedSlots(bookedSlots)
                .cancelledSlots(cancelledSlots)
                .build();
    }
} 