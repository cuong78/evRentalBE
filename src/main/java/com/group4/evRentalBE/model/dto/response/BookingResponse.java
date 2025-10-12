package com.group4.evRentalBE.model.dto.response;

import com.group4.evRentalBE.model.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private String id;
    private CustomerResponse customer;
    private RentalStationResponse station;
    private VehicleTypeResponse type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Booking.BookingStatus status;
    private Double depositAmount;
    private Double rentalFee;
    private Double totalCost;
    private Long rentalDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Payment information
    private Double totalPaid;
    private Double remainingAmount;
    private Boolean isFullyPaid;
    private List<PaymentResponse> payments;
}
