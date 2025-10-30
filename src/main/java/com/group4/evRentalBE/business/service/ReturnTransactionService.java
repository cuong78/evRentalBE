package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.ReturnTransactionRequest;
import com.group4.evRentalBE.business.dto.response.ReturnTransactionResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReturnTransactionService {
    ReturnTransactionResponse createReturnTransaction(ReturnTransactionRequest returnTransactionRequest);
    ReturnTransactionResponse getReturnTransactionByBookingId(String bookingId);

    List<ReturnTransactionResponse> getAllReturnTransactions();

    List<ReturnTransactionResponse> getReturnTransactionsFiltered(Long stationId, Long vehicleTypeId,
                                                                  LocalDate startDate, LocalDate endDate);
}