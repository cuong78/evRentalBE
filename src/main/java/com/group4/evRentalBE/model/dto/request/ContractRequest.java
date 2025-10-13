package com.group4.evRentalBE.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractRequest {

    @NotNull(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotBlank(message = "CCCD is required")
    private String cccd;

    private String conditionNotes;

    @NotBlank(message = "Signature photo is required")
    private String signaturePhoto;

    @NotBlank(message = "Vehicle photo is required")
    private String vehiclePhoto;
}