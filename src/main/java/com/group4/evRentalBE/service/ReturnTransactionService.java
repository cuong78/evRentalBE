package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.request.ReturnTransactionRequest;
import com.group4.evRentalBE.model.dto.response.ReturnTransactionResponse;

public interface ReturnTransactionService {
    ReturnTransactionResponse createReturnTransaction(ReturnTransactionRequest returnTransactionRequest);
    ReturnTransactionResponse getReturnTransactionByBookingId(String bookingId);
}