package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.ContractRequest;
import com.group4.evRentalBE.business.dto.response.ContractResponse;

public interface ContractService {
    ContractResponse createContract(ContractRequest contractRequest);
    ContractResponse getContractByBookingId(String bookingId);
}