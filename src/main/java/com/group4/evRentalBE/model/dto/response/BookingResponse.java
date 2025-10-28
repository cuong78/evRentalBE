package com.group4.evRentalBE.model.dto.response;

import com.group4.evRentalBE.model.entity.Booking;
import com.group4.evRentalBE.model.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String id;
    
    // User information (trực quan hơn)
    private Long userId;
    private String userPhone;
    private String username;
    
    // Station information (trực quan hơn)
    private Long stationId;
    private String stationName;
    private String stationAddress;
    
    // Vehicle Type information (trực quan hơn)
    private Long typeId;
    private String typeName;
    
    // Vehicle information (chỉ có khi status = ACTIVE hoặc COMPLETED)
    private Long vehicleId;
    private String vehicleName;
    
    private LocalDate startDate;
    private LocalDate endDate;
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