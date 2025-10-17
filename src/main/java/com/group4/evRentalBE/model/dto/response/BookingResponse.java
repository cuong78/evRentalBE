package com.group4.evRentalBE.model.dto.response;

import com.group4.evRentalBE.model.entity.Booking;
import com.group4.evRentalBE.model.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String id;
    private Long userId;
    private Long stationId;
    private Long typeId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double totalPayment;
    private Booking.BookingStatus status;
    private Payment.PaymentMethod paymentMethod;
    private LocalDateTime paymentExpiryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional calculated fields for frontend convenience
    private Long rentalDays;
    private Boolean isPaymentExpired;
    private Boolean canCancel;
    private Double totalPaid;
    private Boolean isFullyPaid;
}