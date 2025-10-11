package com.group4.evRentalBE.model.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleTypeResponse {
    private Long id;
    private String name;
    private Double depositAmount;
    private Double rentalRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
