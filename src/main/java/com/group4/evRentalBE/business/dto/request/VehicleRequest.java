package com.group4.evRentalBE.business.dto.request;

import com.group4.evRentalBE.domain.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotNull(message = "Vehicle type ID is required")
    private Long typeId;

    @NotNull(message = "Rental station ID is required")
    private Long stationId;

    @NotNull(message = "Vehicle status is required")
    private Vehicle.VehicleStatus status;

    private String conditionNotes;

    private String photos;
}