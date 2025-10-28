package com.group4.evRentalBE.business.mapper;

import com.group4.evRentalBE.business.dto.request.VehicleTypeRequest;
import com.group4.evRentalBE.business.dto.response.VehicleTypeResponse;
import com.group4.evRentalBE.domain.entity.VehicleType;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypeMapper {

    public VehicleTypeResponse toResponse(VehicleType vehicleType) {
        if (vehicleType == null) {
            return null;
        }

        return VehicleTypeResponse.builder()
                .id(vehicleType.getId())
                .name(vehicleType.getName())
                .depositAmount(vehicleType.getDepositAmount())
                .rentalRate(vehicleType.getRentalRate())

                .build();
    }

    // Có thể thêm các method mapping khác sau này
    public VehicleType toEntity(VehicleTypeRequest request) {
        return VehicleType.builder()
                .name(request.getName())
                .depositAmount(request.getDepositAmount())
                .rentalRate(request.getRentalRate())
                .build();
    }
}