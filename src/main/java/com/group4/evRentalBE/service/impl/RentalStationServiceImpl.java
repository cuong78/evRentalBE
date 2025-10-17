package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ConflictException;
import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.mapper.RentalStationMapper;
import com.group4.evRentalBE.model.dto.request.RentalStationRequest;
import com.group4.evRentalBE.model.dto.response.RentalStationResponse;
import com.group4.evRentalBE.model.entity.RentalStation;
import com.group4.evRentalBE.model.entity.User;
import com.group4.evRentalBE.repository.RentalStationRepository;
import com.group4.evRentalBE.repository.UserRepository;
import com.group4.evRentalBE.service.RentalStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalStationServiceImpl implements RentalStationService {

    private final RentalStationRepository rentalStationRepository;
    private final UserRepository userRepository;
    private final RentalStationMapper rentalStationMapper;

    @Override
    public RentalStationResponse createRentalStation(RentalStationRequest rentalStationRequest) {
        // Check if station with same city and address already exists
        if (rentalStationRepository.existsByCityAndAddress(
                rentalStationRequest.getCity(), rentalStationRequest.getAddress())) {
            throw new ConflictException("Rental station already exists in this location");
        }

        // Validate admin user exists if provided
        User adminUser = null;
        if (rentalStationRequest.getAdminId() != null) {
            adminUser = userRepository.findById(rentalStationRequest.getAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with id: " + rentalStationRequest.getAdminId()));
            
            // Check if user has ADMIN role
            if (!adminUser.hasRole("ADMIN")) {
                throw new ConflictException("User does not have admin role");
            }
        }

        RentalStation rentalStation = rentalStationMapper.toEntity(rentalStationRequest, adminUser);
        RentalStation savedRentalStation = rentalStationRepository.save(rentalStation);

        return rentalStationMapper.toResponse(savedRentalStation);
    }

    @Override
    public List<RentalStationResponse> getAllRentalStations() {
        return rentalStationRepository.findAll().stream()
                .map(rentalStationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RentalStationResponse getRentalStationById(Long id) {
        RentalStation rentalStation = rentalStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + id));
        return rentalStationMapper.toResponse(rentalStation);
    }

    @Override
    public RentalStationResponse updateRentalStation(Long id, RentalStationRequest rentalStationRequest) {
        RentalStation rentalStation = rentalStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + id));

        // Check if updating to an existing location
        if (!rentalStation.getCity().equals(rentalStationRequest.getCity()) ||
                !rentalStation.getAddress().equals(rentalStationRequest.getAddress())) {
            if (rentalStationRepository.existsByCityAndAddress(
                    rentalStationRequest.getCity(), rentalStationRequest.getAddress())) {
                throw new ConflictException("Another rental station already exists in this location");
            }
        }

        // Update basic fields
        rentalStation.setCity(rentalStationRequest.getCity());
        rentalStation.setAddress(rentalStationRequest.getAddress());

        // Update admin user if provided
        if (rentalStationRequest.getAdminId() != null) {
            User adminUser = userRepository.findById(rentalStationRequest.getAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with id: " + rentalStationRequest.getAdminId()));
            
            // Check if user has ADMIN role
            if (!adminUser.hasRole("ADMIN")) {
                throw new ConflictException("User does not have admin role");
            }
            
            rentalStation.setAdminUser(adminUser);
        } else {
            rentalStation.setAdminUser(null);
        }

        RentalStation updatedRentalStation = rentalStationRepository.save(rentalStation);
        return rentalStationMapper.toResponse(updatedRentalStation);
    }

    @Override
    public void deleteRentalStation(Long id) {
        RentalStation rentalStation = rentalStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + id));

        // Check if station has staff users
        if (!rentalStation.getStaffUsers().isEmpty()) {
            throw new ConflictException("Cannot delete rental station. There are staff members associated with this station.");
        }

        // Check if station has vehicles
        if (!rentalStation.getVehicles().isEmpty()) {
            throw new ConflictException("Cannot delete rental station. There are vehicles associated with this station.");
        }

        // Check if station has bookings
        if (!rentalStation.getBookings().isEmpty()) {
            throw new ConflictException("Cannot delete rental station. There are bookings associated with this station.");
        }

        rentalStationRepository.delete(rentalStation);
    }

    @Override
    public List<RentalStationResponse> getRentalStationsByCity(String city) {
        List<RentalStation> rentalStations = rentalStationRepository.findByCity(city);
        return rentalStations.stream()
                .map(rentalStationMapper::toResponse)
                .collect(Collectors.toList());
    }


}