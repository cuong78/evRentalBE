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
public class CancelBookingResponse {
    private String bookingId;
    private String message;
    private Double refundAmount;
    private Double refundPercentage;
    private String refundReason;
    private LocalDateTime cancelledAt;
    private Long walletBalance;
}
