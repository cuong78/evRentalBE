package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.infrastructure.exception.exceptions.ConflictException;
import com.group4.evRentalBE.infrastructure.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.business.mapper.VehicleTypeMapper;
import com.group4.evRentalBE.business.dto.request.VehicleTypeRequest;
import com.group4.evRentalBE.business.dto.response.VehicleTypeResponse;
import com.group4.evRentalBE.business.dto.response.VehicleTypeAvailabilityResponse;
import com.group4.evRentalBE.domain.entity.Vehicle;
import com.group4.evRentalBE.domain.entity.VehicleType;
import com.group4.evRentalBE.domain.repository.VehicleTypeRepository;
import com.group4.evRentalBE.domain.repository.VehicleRepository;
import com.group4.evRentalBE.domain.repository.RentalStationRepository;
import com.group4.evRentalBE.domain.repository.BookingRepository;
import com.group4.evRentalBE.business.service.VehicleTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleTypeServiceImpl implements VehicleTypeService {

    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleTypeMapper vehicleTypeMapper;
    private final VehicleRepository vehicleRepository;
    private final RentalStationRepository rentalStationRepository;
    private final BookingRepository bookingRepository;

    @Override
    public VehicleTypeResponse createVehicleType(VehicleTypeRequest vehicleTypeRequest) {
        // Check if vehicle type name already exists
        if (vehicleTypeRepository.existsByName(vehicleTypeRequest.getName())) {
            throw new ConflictException("Vehicle type name already exists");
        }

        VehicleType vehicleType = vehicleTypeMapper.toEntity(vehicleTypeRequest);
        VehicleType savedVehicleType = vehicleTypeRepository.save(vehicleType);

        return vehicleTypeMapper.toResponse(savedVehicleType);
    }

    @Override
    public List<VehicleTypeResponse> getAllVehicleTypes() {
        return vehicleTypeRepository.findAll().stream()
                .map(vehicleTypeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleTypeResponse getVehicleTypeById(Long id) {
        VehicleType vehicleType = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleType not found with id: " + id));
        return vehicleTypeMapper.toResponse(vehicleType);
    }

    @Override
    public VehicleTypeResponse updateVehicleType(Long id, VehicleTypeRequest vehicleTypeRequest) {
        VehicleType vehicleType = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleType not found with id: " + id));

        // Check if the new name is already taken by another vehicle type
        if (!vehicleType.getName().equals(vehicleTypeRequest.getName()) &&
                vehicleTypeRepository.existsByName(vehicleTypeRequest.getName())) {
            throw new ConflictException("Vehicle type name already exists");
        }

        vehicleType.setName(vehicleTypeRequest.getName());
        vehicleType.setDepositAmount(vehicleTypeRequest.getDepositAmount());
        vehicleType.setRentalRate(vehicleTypeRequest.getRentalRate());

        VehicleType updatedVehicleType = vehicleTypeRepository.save(vehicleType);
        return vehicleTypeMapper.toResponse(updatedVehicleType);
    }

    @Override
    public void deleteVehicleType(Long id) {
        VehicleType vehicleType = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleType not found with id: " + id));

        // Check if there are vehicles using this type
        if (vehicleType.getVehicles() != null && !vehicleType.getVehicles().isEmpty()) {
            throw new ConflictException("Cannot delete vehicle type. There are vehicles associated with this type.");
        }

        if (vehicleType.getBookings() != null && !vehicleType.getBookings().isEmpty()) {
            throw new ConflictException("Cannot delete vehicle type. There are bookings associated with this type.");
        }

        vehicleTypeRepository.delete(vehicleType);
    }

    @Override
    public boolean existsByName(String name) {
        return vehicleTypeRepository.existsByName(name);
    }

    @Override
    public List<VehicleTypeAvailabilityResponse> getVehicleTypesByStation(Long stationId) {
        // Check if station exists
        if (!rentalStationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("Rental station not found with id: " + stationId);
        }

        // Get all vehicles at the station
        List<Vehicle> stationVehicles = vehicleRepository.findByStationId(stationId);
        
        // Group vehicles by type and count available ones
        Map<VehicleType, Long> availableCountByType = stationVehicles.stream()
                .filter(Vehicle::isAvailable)
                .collect(Collectors.groupingBy(Vehicle::getType, Collectors.counting()));

        // Get all vehicle types that have vehicles at this station
        List<VehicleType> vehicleTypesAtStation = stationVehicles.stream()
                .map(Vehicle::getType)
                .distinct()
                .collect(Collectors.toList());

        // Build response with availability info
        return vehicleTypesAtStation.stream()
                .map(type -> VehicleTypeAvailabilityResponse.builder()
                        .id(type.getId())
                        .name(type.getName())
                        .depositAmount(type.getDepositAmount())
                        .rentalRate(type.getRentalRate())
                        .availableCount(availableCountByType.getOrDefault(type, 0L).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleTypeAvailabilityResponse> getVehicleTypesByStationAndDateRange(Long stationId, String startDate, String endDate) {
        // Check if station exists
        if (!rentalStationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("Rental station not found with id: " + stationId);
        }

        // Parse date strings to LocalDateTime
        LocalDateTime[] dateTimeRange = parseDateRange(startDate, endDate);
        LocalDateTime startDateTime = dateTimeRange[0];
        LocalDateTime endDateTime = dateTimeRange[1];

        // Validate date range
        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Get all vehicles at the station
        List<Vehicle> stationVehicles = vehicleRepository.findByStationId(stationId);
        
        // Group vehicles by type
        Map<VehicleType, List<Vehicle>> vehiclesByType = stationVehicles.stream()
                .filter(vehicle -> vehicle.getStatus() == Vehicle.VehicleStatus.AVAILABLE)
                .collect(Collectors.groupingBy(Vehicle::getType));

        // Get all vehicle types that have vehicles at this station
        List<VehicleType> vehicleTypesAtStation = stationVehicles.stream()
                .map(Vehicle::getType)
                .distinct()
                .collect(Collectors.toList());

        // Calculate available count for each type considering bookings
        return vehicleTypesAtStation.stream()
                .map(type -> calculateAvailabilityForType(type, stationId, vehiclesByType, startDateTime, endDateTime))
                .collect(Collectors.toList());
    }

    private LocalDateTime[] parseDateRange(String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new LocalDateTime[]{
                LocalDateTime.parse(startDate, formatter),
                LocalDateTime.parse(endDate, formatter)
            };
        } catch (Exception e) {
            try {
                return new LocalDateTime[]{
                    LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                };
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid date format. Please use 'yyyy-MM-dd' or 'yyyy-MM-dd HH:mm:ss'");
            }
        }
    }

    private VehicleTypeAvailabilityResponse calculateAvailabilityForType(
            VehicleType type, Long stationId, Map<VehicleType, List<Vehicle>> vehiclesByType, 
            LocalDateTime startDateTime, LocalDateTime endDateTime) {
        
        // Total available vehicles of this type at station
        int totalAvailable = vehiclesByType.getOrDefault(type, List.of()).size();
        
        // Count overlapping bookings for this type in the date range
        int bookedCount = bookingRepository.findOverlappingBookings(stationId, type.getId(), startDateTime, endDateTime).size();
        
        // Available count = total available - booked count (cannot be negative)
        int availableCount = Math.max(0, totalAvailable - bookedCount);
        
        return VehicleTypeAvailabilityResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .depositAmount(type.getDepositAmount())
                .rentalRate(type.getRentalRate())
                .availableCount(availableCount)
                .build();
    }

}