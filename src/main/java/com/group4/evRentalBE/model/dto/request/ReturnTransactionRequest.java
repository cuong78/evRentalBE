package com.group4.evRentalBE.model.dto.request;

import com.group4.evRentalBE.model.entity.ReturnTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnTransactionRequest {

    @NotNull(message = "Booking ID is required")
    private String bookingId;

    private String conditionNotes;
    private String photos;

    @NotNull(message = "Refund method is required")
    private ReturnTransaction.RefundMethod refundMethod;

    // Additional fee fields
    private Double damageFee;
    private Double cleaningFee;
    private Boolean isLateReturn;
}