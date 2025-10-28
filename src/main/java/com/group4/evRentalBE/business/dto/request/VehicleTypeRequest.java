package com.group4.evRentalBE.business.dto.request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTypeRequest {
    @NotBlank(message = "Vehicle type name is required")
    private String name;

    @NotNull(message = "Deposit amount is required")
    @Positive(message = "Deposit amount must be positive")
    private Double depositAmount;

    @NotNull(message = "Rental rate is required")
    @Positive(message = "Rental rate must be positive")
    private Double rentalRate;
}
