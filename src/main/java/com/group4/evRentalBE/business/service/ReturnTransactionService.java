package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.ReturnTransactionRequest;
import com.group4.evRentalBE.business.dto.response.ReturnTransactionResponse;

public interface ReturnTransactionService {
    ReturnTransactionResponse createReturnTransaction(ReturnTransactionRequest returnTransactionRequest);
    ReturnTransactionResponse getReturnTransactionByBookingId(String bookingId);
}