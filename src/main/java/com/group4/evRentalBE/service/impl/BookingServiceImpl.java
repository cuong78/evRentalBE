package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ConflictException;
import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.mapper.BookingMapper;
import com.group4.evRentalBE.model.dto.request.BookingRequest;
import com.group4.evRentalBE.model.dto.response.BookingResponse;
import com.group4.evRentalBE.model.entity.*;
import com.group4.evRentalBE.repository.*;
import com.group4.evRentalBE.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final RentalStationRepository rentalStationRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        // Get current authenticated customer
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for current user"));

        // Validate station exists
        RentalStation station = rentalStationRepository.findById(bookingRequest.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("RentalStation not found with id: " + bookingRequest.getStationId()));

        // Validate vehicle type exists
        VehicleType vehicleType = vehicleTypeRepository.findById(bookingRequest.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("VehicleType not found with id: " + bookingRequest.getTypeId()));

        // Validate date range
        if (bookingRequest.getStartDate().isAfter(bookingRequest.getEndDate())) {
            throw new ConflictException("Start date must be before end date");
        }

        if (bookingRequest.getStartDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new ConflictException("Start date must be in the future");
        }

        // Check vehicle type availability
        if (!checkVehicleTypeAvailability(bookingRequest.getStationId(), bookingRequest.getTypeId(),
                bookingRequest.getStartDate().toString(), bookingRequest.getEndDate().toString())) {
            throw new ConflictException("No available vehicles of this type for the selected dates");
        }

        // Check if customer has active booking
        List<Booking> activeBookings = bookingRepository.findByCustomerId(customer.getId()).stream()
                .filter(Booking::isActive)
                .collect(Collectors.toList());

        if (!activeBookings.isEmpty()) {
            throw new ConflictException("Customer already has an active booking");
        }

        // Create booking
        Booking booking = bookingMapper.toEntity(bookingRequest, customer, station, vehicleType);

        // Generate unique booking ID
        booking.setId(generateBookingId());

        // Calculate initial payment (deposit)
        booking.setDepositPaid(vehicleType.getDepositAmount());
        booking.setTotalInitialPayment(vehicleType.getDepositAmount());

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return bookingMapper.toResponse(booking);
    }

    @Override
    public List<BookingResponse> getCustomerBookings() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for current user"));

        return bookingRepository.findByCustomerId(customer.getId()).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getStationBookings(Long stationId) {
        if (!rentalStationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("RentalStation not found with id: " + stationId);
        }

        return bookingRepository.findByStationId(stationId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public BookingResponse cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // ✅ SỬ DỤNG BUSINESS METHOD để cancel booking
        if (!booking.canCancel()) {
            throw new ConflictException("Booking cannot be cancelled. Cancellation must be at least 24 hours before start date.");
        }

        booking.cancel();
        Booking updatedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(updatedBooking);
    }



    @Override
    public Boolean checkVehicleTypeAvailability(Long stationId, Long typeId, String startDate, String endDate) {
        int availableCount = getAvailableVehicleCount(stationId, typeId, startDate, endDate);
        return availableCount > 0;
    }

    @Override
    public Integer getAvailableVehicleCount(Long stationId, Long typeId, String startDate, String endDate) {
        LocalDateTime[] dateTimeRange = parseDateRange(startDate, endDate);
        LocalDateTime startDateTime = dateTimeRange[0];
        LocalDateTime endDateTime = dateTimeRange[1];

        // Get total vehicles of this type at station
        List<Vehicle> stationVehicles = vehicleRepository.findByStationIdAndTypeId(stationId, typeId);
        int totalVehicles = stationVehicles.size();

        // Get overlapping bookings
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(stationId, typeId, startDateTime, endDateTime);
        int bookedCount = overlappingBookings.size();

        return Math.max(0, totalVehicles - bookedCount);
    }

    private String generateBookingId() {
        return "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private LocalDateTime[] parseDateRange(String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new LocalDateTime[]{
                    LocalDateTime.parse(startDate, formatter),
                    LocalDateTime.parse(endDate, formatter)
            };
        } catch (Exception e) {
            try {
                return new LocalDateTime[]{
                        LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                };
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid date format. Please use 'yyyy-MM-dd' or 'yyyy-MM-dd HH:mm:ss'");
            }
        }
    }
}