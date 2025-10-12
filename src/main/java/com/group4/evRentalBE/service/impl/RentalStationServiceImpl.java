package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ConflictException;
import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.mapper.RentalStationMapper;
import com.group4.evRentalBE.model.dto.request.RentalStationRequest;
import com.group4.evRentalBE.model.dto.response.RentalStationResponse;
import com.group4.evRentalBE.model.entity.Admin;
import com.group4.evRentalBE.model.entity.RentalStation;
import com.group4.evRentalBE.repository.AdminRepository;
import com.group4.evRentalBE.repository.RentalStationRepository;
import com.group4.evRentalBE.service.RentalStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalStationServiceImpl implements RentalStationService {

    private final RentalStationRepository rentalStationRepository;
    private final AdminRepository adminRepository;
    private final RentalStationMapper rentalStationMapper;

    @Override
    public RentalStationResponse createRentalStation(RentalStationRequest rentalStationRequest) {
        // Check if station with same city and address already exists
        if (rentalStationRepository.existsByCityAndAddress(
                rentalStationRequest.getCity(), rentalStationRequest.getAddress())) {
            throw new ConflictException("Rental station already exists in this location");
        }

        // Validate admin exists if provided
        Admin admin = null;
        if (rentalStationRequest.getAdminId() != null) {
            admin = adminRepository.findById(rentalStationRequest.getAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + rentalStationRequest.getAdminId()));
        }

        RentalStation rentalStation = rentalStationMapper.toEntity(rentalStationRequest, admin);
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

        // Update admin if provided
        if (rentalStationRequest.getAdminId() != null) {
            Admin admin = adminRepository.findById(rentalStationRequest.getAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + rentalStationRequest.getAdminId()));
            rentalStation.setAdmin(admin);
        } else {
            rentalStation.setAdmin(null);
        }

        RentalStation updatedRentalStation = rentalStationRepository.save(rentalStation);
        return rentalStationMapper.toResponse(updatedRentalStation);
    }

    @Override
    public void deleteRentalStation(Long id) {
        RentalStation rentalStation = rentalStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + id));

        // Check if station has staff members
        if (!rentalStation.getStaffMembers().isEmpty()) {
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

    @Override
    public RentalStationResponse assignAdminToStation(Long stationId, Long adminId) {
        RentalStation rentalStation = rentalStationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + stationId));

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));

        // ✅ SỬ DỤNG BUSINESS METHOD từ entity Admin
        admin.manageStation(rentalStation);

        Admin savedAdmin = adminRepository.save(admin);
        return rentalStationMapper.toResponse(rentalStation);
    }

    @Override
    public RentalStationResponse removeAdminFromStation(Long stationId) {
        RentalStation rentalStation = rentalStationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + stationId));

        if (rentalStation.getAdmin() == null) {
            throw new ConflictException("Rental station does not have an admin assigned");
        }

        Admin admin = rentalStation.getAdmin();

        // ✅ SỬ DỤNG BUSINESS METHOD từ entity Admin
        admin.removeStation(rentalStation);

        adminRepository.save(admin);
        return rentalStationMapper.toResponse(rentalStation);
    }

    @Override
    public List<RentalStationResponse> getStationsWithoutAdmin() {
        List<RentalStation> stations = rentalStationRepository.findAll().stream()
                .filter(station -> station.getAdmin() == null)
                .collect(Collectors.toList());

        return stations.stream()
                .map(rentalStationMapper::toResponse)
                .collect(Collectors.toList());
    }
}