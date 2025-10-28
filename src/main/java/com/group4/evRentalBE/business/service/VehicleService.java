package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.VehicleRequest;
import com.group4.evRentalBE.business.dto.response.VehicleAvailabilityResponse;
import com.group4.evRentalBE.business.dto.response.VehicleResponse;

import java.time.LocalDate;
import java.util.List;

public interface VehicleService {
    VehicleResponse createVehicle(VehicleRequest vehicleRequest);
    List<VehicleResponse> getAllVehicles();
    VehicleResponse getVehicleById(Long id);
    VehicleResponse updateVehicle(Long id, VehicleRequest vehicleRequest);
    void deleteVehicle(Long id);
    List<VehicleResponse> getVehiclesByStation(Long stationId);
    List<VehicleResponse> getVehiclesByType(Long typeId);

    List<VehicleResponse> getVehiclesByStationAndType(Long stationId, Long typeId);
    
    // New method with date range search
    VehicleAvailabilityResponse searchAvailableVehicles(Long stationId, LocalDate startDate, LocalDate endDate);

    byte[] generateQRCodePDFByStation(Long stationId) throws Exception;

}
