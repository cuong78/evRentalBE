package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.model.dto.request.BookingRequest;
import com.group4.evRentalBE.model.dto.response.BookingResponse;
import com.group4.evRentalBE.model.entity.*;
import com.group4.evRentalBE.repository.*;
import com.group4.evRentalBE.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RentalStationRepository rentalStationRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        // Get authenticated user from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // Verify user exists in database (optional safety check)
        User authenticatedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user has CUSTOMER role
        if (!authenticatedUser.hasRole("CUSTOMER")) {
            throw new ResourceNotFoundException("User does not have customer role");
        }

        RentalStation station = rentalStationRepository.findById(bookingRequest.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Rental station not found"));

        VehicleType vehicleType = vehicleTypeRepository.findById(bookingRequest.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle type not found"));

        // Create booking
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setUser(authenticatedUser);
        booking.setStation(station);
        booking.setType(vehicleType);
        booking.setStartDate(bookingRequest.getStartDate());
        booking.setEndDate(bookingRequest.getEndDate());
        booking.setPaymentMethod(Payment.PaymentMethod.VNPAY);

        // PrePersist will set createdAt, updatedAt, paymentExpiryTime and calculate totalPayment
        Booking savedBooking = bookingRepository.save(booking);

        return mapToBookingResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return mapToBookingResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingsByCustomer(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return bookingRepository.findByUserUserId(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cancelExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndPaymentExpiryTimeBefore(
                        Booking.BookingStatus.PENDING,
                        LocalDateTime.now()
                );

        for (Booking booking : expiredBookings) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getUserId())
                .stationId(booking.getStation().getId())
                .typeId(booking.getType().getId())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .totalPayment(booking.getTotalPayment())
                .status(booking.getStatus())
                .paymentExpiryTime(booking.getPaymentExpiryTime())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
