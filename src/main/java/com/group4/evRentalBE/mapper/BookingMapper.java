package com.group4.evRentalBE.mapper;

import com.group4.evRentalBE.model.dto.request.BookingRequest;
import com.group4.evRentalBE.model.dto.response.*;
import com.group4.evRentalBE.model.entity.Booking;
import com.group4.evRentalBE.model.entity.Customer;
import com.group4.evRentalBE.model.entity.RentalStation;
import com.group4.evRentalBE.model.entity.VehicleType;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        // Map customer
        CustomerResponse customerResponse = null;
        if (booking.getCustomer() != null && booking.getCustomer().getUser() != null) {
            customerResponse = new CustomerResponse(
                    booking.getCustomer().getUser().getUserId(),
                    booking.getCustomer().getUser().getUsername(),
                    booking.getCustomer().getUser().getEmail(),
                    booking.getCustomer().getUser().getPhone()
            );
        }

        // Map station
        RentalStationResponse stationResponse = null;
        if (booking.getStation() != null) {
            stationResponse = new RentalStationResponse(
                    booking.getStation().getId(),
                    booking.getStation().getCity(),
                    booking.getStation().getAddress()
            );
        }

        // Map vehicle type
        VehicleTypeResponse typeResponse = null;
        if (booking.getType() != null) {
            typeResponse = new VehicleTypeResponse(
                    booking.getType().getId(),
                    booking.getType().getName(),
                    booking.getType().getDepositAmount(),
                    booking.getType().getRentalRate()
            );
        }

        // Map payments
        var paymentResponses = booking.getPayments().stream()
                .map(payment -> new PaymentResponse(
                        payment.getId(),
                        payment.getType().toString(),
                        payment.getStatus().toString(),
                        payment.getAmount(),
                        payment.getPaymentDate()
                ))
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .customer(customerResponse)
                .station(stationResponse)
                .type(typeResponse)
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .status(booking.getStatus())
                .depositAmount(booking.getType().getDepositAmount())
                .rentalFee(booking.getRentalFee())
                .totalCost(booking.calculateTotalCost())
                .rentalDays(booking.getRentalDays())
                .totalPaid(booking.getTotalPaid())
                .remainingAmount(booking.getRemainingAmount())
                .isFullyPaid(booking.isFullyPaid())
                .payments(paymentResponses)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public Booking toEntity(BookingRequest request, Customer customer, RentalStation station, VehicleType type) {
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setStation(station);
        booking.setType(type);
        // Convert LocalDate to LocalDateTime (assuming start of day for startDate and end of day for endDate)
        booking.setStartDate(request.getStartDate().atTime(0, 0));
        booking.setEndDate(request.getEndDate().atTime(23, 59, 59));
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setRentalFee(type.getRentalRate());
        return booking;
    }
}