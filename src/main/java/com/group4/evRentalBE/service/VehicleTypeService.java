package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.request.VehicleTypeRequest;
import com.group4.evRentalBE.model.dto.response.VehicleTypeResponse;

import java.util.List;

public interface VehicleTypeService {
    VehicleTypeResponse createVehicleType(VehicleTypeRequest vehicleTypeRequest);
    List<VehicleTypeResponse> getAllVehicleTypes();
    VehicleTypeResponse getVehicleTypeById(Long id);
    VehicleTypeResponse updateVehicleType(Long id, VehicleTypeRequest vehicleTypeRequest);
    void deleteVehicleType(Long id);
    boolean existsByName(String name);
}
