package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.RentalStationRequest;
import com.group4.evRentalBE.business.dto.response.RentalStationResponse;

import java.util.List;

public interface RentalStationService {
    RentalStationResponse createRentalStation(RentalStationRequest rentalStationRequest);
    List<RentalStationResponse> getAllRentalStations();
    RentalStationResponse getRentalStationById(Long id);
    RentalStationResponse updateRentalStation(Long id, RentalStationRequest rentalStationRequest);
    void deleteRentalStation(Long id);
    List<RentalStationResponse> getRentalStationsByCity(String city);


}