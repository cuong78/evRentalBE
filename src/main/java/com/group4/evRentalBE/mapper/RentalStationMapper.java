package com.group4.evRentalBE.mapper;

import com.group4.evRentalBE.model.dto.request.RentalStationRequest;
import com.group4.evRentalBE.model.dto.response.*;
import com.group4.evRentalBE.model.entity.RentalStation;
import com.group4.evRentalBE.model.entity.User;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RentalStationMapper {

    public RentalStationResponse toResponse(RentalStation rentalStation) {
        if (rentalStation == null) {
            return null;
        }

        // Map admin user
        AdminResponse adminResponse = null;
        if (rentalStation.getAdminUser() != null) {
            adminResponse = new AdminResponse(
                    rentalStation.getAdminUser().getUserId(),
                    rentalStation.getAdminUser().getUsername()
            );
        }

        // Map staff users
        List<StaffResponse> staffResponses = rentalStation.getStaffUsers().stream()
                .filter(user -> user.hasRole("STAFF"))
                .map(user -> new StaffResponse(
                        user.getUserId(),
                        user.getUsername(),
                        "STAFF"
                ))
                .collect(Collectors.toList());

        // Map vehicles
        List<VehicleResponse> vehicleResponses = rentalStation.getVehicles().stream()
                .map(vehicle -> new VehicleResponse(
                        vehicle.getId(),
                        vehicle.getConditionNotes(),
                        vehicle.getStatus()
                ))
                .collect(Collectors.toList());

        // Tính thống kê
        int availableVehicles = (int) rentalStation.getVehicles().stream()
                .filter(vehicle -> vehicle.isAvailable())
                .count();

        RentalStationResponse response = new RentalStationResponse(
                rentalStation.getId(),
                rentalStation.getCity(),
                rentalStation.getAddress(),
                adminResponse,
                staffResponses,
                vehicleResponses,
                rentalStation.getCreatedAt(),
                rentalStation.getUpdatedAt(),
                rentalStation.getStaffUsers().size(),
                rentalStation.getVehicles().size(),
                availableVehicles
        );

        return response;
    }

    public RentalStation toEntity(RentalStationRequest request, User adminUser) {
        return RentalStation.builder()
                .city(request.getCity())
                .address(request.getAddress())
                .adminUser(adminUser)
                .build();
    }
}