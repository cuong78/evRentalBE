package com.group4.evRentalBE.business.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleTypeAvailabilityResponse {
    private Long id;
    private String name;
    private Double depositAmount;
    private Double rentalRate;
    private Integer availableCount;
}