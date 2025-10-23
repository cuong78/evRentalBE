package com.group4.evRentalBE.mapper;

import com.group4.evRentalBE.model.dto.request.VehicleRequest;
import com.group4.evRentalBE.model.dto.response.RentalStationResponse;
import com.group4.evRentalBE.model.dto.response.VehicleResponse;
import com.group4.evRentalBE.model.dto.response.VehicleTypeResponse;
import com.group4.evRentalBE.model.entity.Vehicle;
import com.group4.evRentalBE.model.entity.VehicleType;
import com.group4.evRentalBE.model.entity.RentalStation;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        VehicleTypeResponse typeResponse = null;
        if (vehicle.getType() != null) {
            typeResponse = new VehicleTypeResponse(
                    vehicle.getType().getId(),
                    vehicle.getType().getName(),
                    vehicle.getType().getDepositAmount(),
                    vehicle.getType().getRentalRate()
            );
        }

        RentalStationResponse stationResponse = null;
        if (vehicle.getStation() != null) {
            stationResponse = new RentalStationResponse(
                    vehicle.getStation().getId(),
                    vehicle.getStation().getCity(),
                    vehicle.getStation().getAddress()
            );
        }

        return new VehicleResponse(
                vehicle.getId(),
                typeResponse,
                stationResponse,
                vehicle.getStatus(),
                vehicle.getConditionNotes(),
                vehicle.getPhotos(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    public Vehicle toEntity(VehicleRequest request, VehicleType type, RentalStation station) {
        return Vehicle.builder()
                .type(type)
                .station(station)
                .status(request.getStatus())
                .conditionNotes(request.getConditionNotes())
                .photos(request.getPhotos())
                .build();
    }
}