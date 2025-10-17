package com.group4.evRentalBE.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAvailabilityResponse {
    
    private Long stationId;
    private String stationName;
    private LocalDateTime searchStartDate;
    private LocalDateTime searchEndDate;
    private List<VehicleTypeAvailability> vehicleTypes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleTypeAvailability {
        private Long typeId;
        private String typeName;
        private Double depositAmount;
        private Double rentalRate;
        private Integer totalVehicles;
        private Integer availableCount;
        private List<VehicleResponse> availableVehicles;
    }
}
