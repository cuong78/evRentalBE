package com.group4.evRentalBE.model.dto.request;

import com.group4.evRentalBE.model.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Station ID is required")
    private Long stationId;

    @NotNull(message = "Vehicle type ID is required")
    private Long typeId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;
}