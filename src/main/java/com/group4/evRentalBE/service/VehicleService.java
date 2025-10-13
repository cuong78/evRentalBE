package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.request.VehicleRequest;
import com.group4.evRentalBE.model.dto.response.VehicleResponse;

import java.util.List;

public interface VehicleService {
    VehicleResponse createVehicle(VehicleRequest vehicleRequest);
    List<VehicleResponse> getAllVehicles();
    VehicleResponse getVehicleById(Long id);
    VehicleResponse updateVehicle(Long id, VehicleRequest vehicleRequest);
    void deleteVehicle(Long id);
    List<VehicleResponse> getVehiclesByStation(Long stationId);
    List<VehicleResponse> getVehiclesByType(Long typeId);
    List<VehicleResponse> getAvailableVehicles();
    List<VehicleResponse> getAvailableVehiclesByStation(Long stationId);
    List<VehicleResponse> getVehiclesByStationAndType(Long stationId, Long typeId);
}
