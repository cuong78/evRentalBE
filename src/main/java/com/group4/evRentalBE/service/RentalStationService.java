package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.request.RentalStationRequest;
import com.group4.evRentalBE.model.dto.response.RentalStationResponse;

import java.util.List;

public interface RentalStationService {
    RentalStationResponse createRentalStation(RentalStationRequest rentalStationRequest);
    List<RentalStationResponse> getAllRentalStations();
    RentalStationResponse getRentalStationById(Long id);
    RentalStationResponse updateRentalStation(Long id, RentalStationRequest rentalStationRequest);
    void deleteRentalStation(Long id);
    List<RentalStationResponse> getRentalStationsByCity(String city);

    // Business methods
    RentalStationResponse assignAdminToStation(Long stationId, Long adminId);
    RentalStationResponse removeAdminFromStation(Long stationId);
    List<RentalStationResponse> getStationsWithoutAdmin();
}