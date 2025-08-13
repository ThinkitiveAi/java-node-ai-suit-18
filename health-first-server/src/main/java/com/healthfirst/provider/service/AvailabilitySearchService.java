package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.AvailabilitySearchRequest;
import com.healthfirst.provider.dto.AvailabilitySearchResponse;
import com.healthfirst.provider.entity.AppointmentSlot;
import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.entity.ProviderAvailability;
import com.healthfirst.provider.repository.AppointmentSlotRepository;
import com.healthfirst.provider.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilitySearchService {
    
    private final AppointmentSlotRepository slotRepository;
    private final ProviderRepository providerRepository;
    
    public AvailabilitySearchResponse searchAvailability(AvailabilitySearchRequest request) {
        // Build search criteria
        AvailabilitySearchResponse.SearchCriteria searchCriteria = AvailabilitySearchResponse.SearchCriteria.builder()
                .date(request.getDate() != null ? request.getDate().toString() : null)
                .specialization(request.getSpecialization())
                .location(request.getLocation())
                .build();
        
        // Determine date range for search
        LocalDate startDate = request.getDate() != null ? request.getDate() : request.getStartDate();
        LocalDate endDate = request.getDate() != null ? request.getDate() : request.getEndDate();
        
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(7); // Default to 1 week search
        }
        
        // Convert to ZonedDateTime for database query
        ZoneId zoneId = request.getTimezone() != null ? ZoneId.of(request.getTimezone()) : ZoneId.systemDefault();
        ZonedDateTime searchStartTime = startDate.atStartOfDay(zoneId);
        ZonedDateTime searchEndTime = endDate.plusDays(1).atStartOfDay(zoneId);
        
        // Get all available slots in the date range
        List<AppointmentSlot> availableSlots = slotRepository.findAvailableSlotsInTimeRange(
                null, searchStartTime, searchEndTime);
        
        // Filter by appointment type if specified
        if (request.getAppointmentType() != null) {
            availableSlots = availableSlots.stream()
                    .filter(slot -> slot.getAppointmentType().equalsIgnoreCase(request.getAppointmentType()))
                    .collect(Collectors.toList());
        }
        
        // Group slots by provider
        var slotsByProvider = availableSlots.stream()
                .collect(Collectors.groupingBy(AppointmentSlot::getProvider));
        
        // Build search results
        List<AvailabilitySearchResponse.SearchResult> results = slotsByProvider.entrySet().stream()
                .map(entry -> buildSearchResult(entry.getKey(), entry.getValue(), request))
                .filter(result -> result != null) // Filter out results that don't match criteria
                .collect(Collectors.toList());
        
        return AvailabilitySearchResponse.builder()
                .success(true)
                .data(AvailabilitySearchResponse.SearchData.builder()
                        .searchCriteria(searchCriteria)
                        .totalResults(results.size())
                        .results(results)
                        .build())
                .build();
    }
    
    private AvailabilitySearchResponse.SearchResult buildSearchResult(Provider provider, 
                                                                     List<AppointmentSlot> slots,
                                                                     AvailabilitySearchRequest request) {
        // Filter by specialization if specified
        if (request.getSpecialization() != null && 
            !provider.getSpecialization().equalsIgnoreCase(request.getSpecialization())) {
            return null;
        }
        
        // Filter by location if specified
        if (request.getLocation() != null && 
            !matchesLocation(provider, request.getLocation())) {
            return null;
        }
        
        // Filter by insurance acceptance if specified
        if (request.getInsuranceAccepted() != null) {
            slots = slots.stream()
                    .filter(slot -> {
                        var pricing = slot.getAvailability().getPricing();
                        return pricing != null && 
                               pricing.getInsuranceAccepted().equals(request.getInsuranceAccepted());
                    })
                    .collect(Collectors.toList());
        }
        
        // Filter by max price if specified
        if (request.getMaxPrice() != null) {
            slots = slots.stream()
                    .filter(slot -> {
                        var pricing = slot.getAvailability().getPricing();
                        return pricing != null && 
                               pricing.getBaseFee() != null &&
                               pricing.getBaseFee().compareTo(request.getMaxPrice()) <= 0;
                    })
                    .collect(Collectors.toList());
        }
        
        if (slots.isEmpty()) {
            return null;
        }
        
        // Build provider info
        AvailabilitySearchResponse.ProviderInfo providerInfo = AvailabilitySearchResponse.ProviderInfo.builder()
                .id(provider.getId())
                .name(provider.getFirstName() + " " + provider.getLastName())
                .specialization(provider.getSpecialization())
                .yearsOfExperience(provider.getYearsOfExperience())
                .rating(4.5) // TODO: Implement rating system
                .clinicAddress(provider.getClinicAddress().getStreet() + ", " + 
                              provider.getClinicAddress().getCity() + ", " + 
                              provider.getClinicAddress().getState())
                .build();
        
        // Build available slots
        List<AvailabilitySearchResponse.AvailableSlot> availableSlots = slots.stream()
                .map(this::buildAvailableSlot)
                .collect(Collectors.toList());
        
        return AvailabilitySearchResponse.SearchResult.builder()
                .provider(providerInfo)
                .availableSlots(availableSlots)
                .build();
    }
    
    private boolean matchesLocation(Provider provider, String searchLocation) {
        String providerLocation = provider.getClinicAddress().getCity() + ", " + 
                                 provider.getClinicAddress().getState() + " " + 
                                 provider.getClinicAddress().getZip();
        
        return providerLocation.toLowerCase().contains(searchLocation.toLowerCase()) ||
               provider.getClinicAddress().getCity().toLowerCase().contains(searchLocation.toLowerCase()) ||
               provider.getClinicAddress().getState().toLowerCase().contains(searchLocation.toLowerCase()) ||
               provider.getClinicAddress().getZip().contains(searchLocation);
    }
    
    private AvailabilitySearchResponse.AvailableSlot buildAvailableSlot(AppointmentSlot slot) {
        var availability = slot.getAvailability();
        var location = availability.getLocation();
        var pricing = availability.getPricing();
        
        return AvailabilitySearchResponse.AvailableSlot.builder()
                .slotId(slot.getId())
                .date(slot.getSlotStartTime().toLocalDate().toString())
                .startTime(slot.getSlotStartTime().toLocalTime().toString())
                .endTime(slot.getSlotEndTime().toLocalTime().toString())
                .appointmentType(slot.getAppointmentType())
                .location(AvailabilitySearchResponse.LocationInfo.builder()
                        .type(location != null ? location.getType().name() : null)
                        .address(location != null ? location.getAddress() : null)
                        .roomNumber(location != null ? location.getRoomNumber() : null)
                        .build())
                .pricing(AvailabilitySearchResponse.PricingInfo.builder()
                        .baseFee(pricing != null ? pricing.getBaseFee() : null)
                        .insuranceAccepted(pricing != null ? pricing.getInsuranceAccepted() : false)
                        .currency(pricing != null ? pricing.getCurrency() : "USD")
                        .build())
                .specialRequirements(availability.getSpecialRequirements())
                .build();
    }
} 