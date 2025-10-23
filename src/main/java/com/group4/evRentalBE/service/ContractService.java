package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.request.ContractRequest;
import com.group4.evRentalBE.model.dto.response.ContractResponse;

public interface ContractService {
    ContractResponse createContract(ContractRequest contractRequest);
    ContractResponse getContractByBookingId(String bookingId);
}