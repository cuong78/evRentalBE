package com.group4.evRentalBE.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalStationResponse {
    private Long id;
    private String city;
    private String address;
    private AdminResponse admin;
    private List<StaffResponse> staffMembers = new ArrayList<>();
    private List<VehicleResponse> vehicles = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thống kê
    private Integer totalStaff;
    private Integer totalVehicles;
    private Integer availableVehicles;

    public RentalStationResponse(Long id, String city, String address) {
        this.id = id;
        this.city = city;
        this.address = address;
    }
}


