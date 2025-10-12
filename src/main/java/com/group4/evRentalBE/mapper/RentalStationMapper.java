package com.group4.evRentalBE.mapper;

import com.group4.evRentalBE.model.dto.request.RentalStationRequest;
import com.group4.evRentalBE.model.dto.response.*;
import com.group4.evRentalBE.model.entity.RentalStation;
import com.group4.evRentalBE.model.entity.Admin;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RentalStationMapper {

    public RentalStationResponse toResponse(RentalStation rentalStation) {
        if (rentalStation == null) {
            return null;
        }

        // Map admin
        AdminResponse adminResponse = null;
        if (rentalStation.getAdmin() != null) {
            adminResponse = new AdminResponse(
                    rentalStation.getAdmin().getId(),
                    rentalStation.getAdmin().getName()
            );
        }

        // Map staff members
        List<StaffResponse> staffResponses = rentalStation.getStaffMembers().stream()
                .map(staff -> new StaffResponse(
                        staff.getId(),
                        staff.getName(),
                        staff.getRole()
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
                rentalStation.getStaffMembers().size(),
                rentalStation.getVehicles().size(),
                availableVehicles
        );

        return response;
    }

    public RentalStation toEntity(RentalStationRequest request, Admin admin) {
        return RentalStation.builder()
                .city(request.getCity())
                .address(request.getAddress())
                .admin(admin)
                .build();
    }
}