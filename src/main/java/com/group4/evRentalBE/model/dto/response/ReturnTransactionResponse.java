package com.group4.evRentalBE.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnTransactionResponse {
    private Long id;
    private String bookingId;
    private LocalDateTime returnDate;
    private Double additionalFees;
    private Double refundAmount;
    private String conditionNotes;
    private String photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional calculated fields
    private Boolean isLateReturn;
    private Long overdueDays;
    private Double originalDeposit;
    private String refundStatus;
}