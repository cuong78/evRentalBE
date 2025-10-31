package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.ContractRequest;
import com.group4.evRentalBE.business.dto.response.ContractResponse;

import java.time.LocalDate;
import java.util.List;

public interface ContractService {
    ContractResponse createContract(ContractRequest contractRequest);
    ContractResponse getContractByBookingId(String bookingId);
    List<ContractResponse> getContractsFiltered(Long stationId, Long vehicleTypeId, LocalDate startDate, LocalDate endDate);
    List<ContractResponse> getAllContracts();

}