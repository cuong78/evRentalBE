package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.VehicleTypeRequest;
import com.group4.evRentalBE.business.dto.response.VehicleTypeResponse;
import com.group4.evRentalBE.business.dto.response.VehicleTypeAvailabilityResponse;

import java.util.List;

public interface VehicleTypeService {
    VehicleTypeResponse createVehicleType(VehicleTypeRequest vehicleTypeRequest);
    List<VehicleTypeResponse> getAllVehicleTypes();
    VehicleTypeResponse getVehicleTypeById(Long id);
    VehicleTypeResponse updateVehicleType(Long id, VehicleTypeRequest vehicleTypeRequest);
    void deleteVehicleType(Long id);
    boolean existsByName(String name);
    List<VehicleTypeAvailabilityResponse> getVehicleTypesByStation(Long stationId);
    List<VehicleTypeAvailabilityResponse> getVehicleTypesByStationAndDateRange(Long stationId, String startDate, String endDate);
}
