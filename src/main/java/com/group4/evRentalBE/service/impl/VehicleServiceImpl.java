package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ConflictException;
import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.mapper.VehicleMapper;
import com.group4.evRentalBE.model.dto.request.VehicleRequest;
import com.group4.evRentalBE.model.dto.response.VehicleAvailabilityResponse;
import com.group4.evRentalBE.model.dto.response.VehicleResponse;
import com.group4.evRentalBE.model.entity.*;
import com.group4.evRentalBE.repository.VehicleRepository;
import com.group4.evRentalBE.repository.VehicleTypeRepository;
import com.group4.evRentalBE.repository.RentalStationRepository;
import com.group4.evRentalBE.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final RentalStationRepository rentalStationRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    public VehicleResponse createVehicle(VehicleRequest vehicleRequest) {
        // Validate vehicle type exists
        VehicleType vehicleType = vehicleTypeRepository.findById(vehicleRequest.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("VehicleType not found with id: " + vehicleRequest.getTypeId()));

        // Validate rental station exists
        RentalStation rentalStation = rentalStationRepository.findById(vehicleRequest.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + vehicleRequest.getStationId()));

        Vehicle vehicle = vehicleMapper.toEntity(vehicleRequest, vehicleType, rentalStation);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return vehicleMapper.toResponse(savedVehicle);
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    public VehicleResponse updateVehicle(Long id, VehicleRequest vehicleRequest) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        // Validate và cập nhật relationships
        if (vehicleRequest.getTypeId() != null) {
            VehicleType vehicleType = vehicleTypeRepository.findById(vehicleRequest.getTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("VehicleType not found with id: " + vehicleRequest.getTypeId()));
            vehicle.setType(vehicleType);
        }

        if (vehicleRequest.getStationId() != null) {
            RentalStation rentalStation = rentalStationRepository.findById(vehicleRequest.getStationId())
                    .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + vehicleRequest.getStationId()));
            vehicle.setStation(rentalStation);
        }

        if (vehicleRequest.getStatus() != null) {
            vehicle.updateStatus(vehicleRequest.getStatus());
        }

        // Cập nhật các trường khác
        if (vehicleRequest.getConditionNotes() != null) {
            vehicle.setConditionNotes(vehicleRequest.getConditionNotes());
        }
        if (vehicleRequest.getPhotos() != null) {
            vehicle.setPhotos(vehicleRequest.getPhotos());
        }

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(updatedVehicle);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        // Check if vehicle has active contracts
        if (vehicle.getContracts() != null) {
            throw new ConflictException("Cannot delete vehicle. There is an active contract associated with this vehicle.");
        }

        vehicleRepository.delete(vehicle);
    }

    @Override
    public List<VehicleResponse> getVehiclesByStation(Long stationId) {
        List<Vehicle> vehicles = vehicleRepository.findByStationId(stationId);
        return vehicles.stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleResponse> getVehiclesByType(Long typeId) {
        List<Vehicle> vehicles = vehicleRepository.findByTypeId(typeId);
        return vehicles.stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }



    @Override
    public List<VehicleResponse> getVehiclesByStationAndType(Long stationId, Long typeId) {
        // Validate station exists
        if (!rentalStationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("RentalStation not found with id: " + stationId);
        }

        // Validate vehicle type exists
        if (!vehicleTypeRepository.existsById(typeId)) {
            throw new ResourceNotFoundException("VehicleType not found with id: " + typeId);
        }

        List<Vehicle> vehicles = vehicleRepository.findByStationIdAndTypeId(stationId, typeId);
        return vehicles.stream()
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleAvailabilityResponse searchAvailableVehicles(Long stationId, LocalDateTime startDate, LocalDateTime endDate) {
        // Validate dates
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Validate station exists
        RentalStation station = rentalStationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + stationId));

        List<Vehicle> allAvailableVehicles = vehicleRepository.findAvailableVehiclesByStation(
                stationId,
                startDate ,
                endDate
        );

        // Group vehicles by type
        Map<VehicleType, List<Vehicle>> vehiclesByType = allAvailableVehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getType));

        // Build response for each vehicle type
        List<VehicleAvailabilityResponse.VehicleTypeAvailability> vehicleTypeAvailabilities = 
                vehiclesByType.entrySet().stream()
                .map(entry -> {
                    VehicleType type = entry.getKey();
                    List<Vehicle> vehicles = entry.getValue();
                    
                    // Count total vehicles of this type at station
                    long totalVehicles = vehicleRepository.countByStationAndType(
                            stationId,
                            type.getId()
                    );
                    
                    // Map to VehicleResponse
                    List<VehicleResponse> vehicleResponses = vehicles.stream()
                            .map(vehicleMapper::toResponse)
                            .collect(Collectors.toList());
                    
                    return VehicleAvailabilityResponse.VehicleTypeAvailability.builder()
                            .typeId(type.getId())
                            .typeName(type.getName())
                            .depositAmount(type.getDepositAmount())
                            .rentalRate(type.getRentalRate())
                            .totalVehicles((int) totalVehicles)
                            .availableCount(vehicles.size())
                            .availableVehicles(vehicleResponses)
                            .build();
                })
                .sorted((a, b) -> a.getTypeId().compareTo(b.getTypeId()))
                .collect(Collectors.toList());

        return VehicleAvailabilityResponse.builder()
                .stationId(station.getId())
                .stationName(station.getCity() + " - " + station.getAddress())
                .searchStartDate(startDate)
                .searchEndDate(endDate)
                .vehicleTypes(vehicleTypeAvailabilities)
                .build();
    }
}
