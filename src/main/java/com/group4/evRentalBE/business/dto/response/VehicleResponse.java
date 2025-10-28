package com.group4.evRentalBE.business.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.group4.evRentalBE.domain.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class VehicleResponse {
    private Long id;
    private VehicleTypeResponse type;
    private RentalStationResponse station;
    private Vehicle.VehicleStatus status;
    private String conditionNotes;
    private String photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VehicleResponse(Long id, String conditionNotes, Vehicle.VehicleStatus status) {
        this.id = id;
        this.conditionNotes = conditionNotes;
        this.status = status;
    }
}



