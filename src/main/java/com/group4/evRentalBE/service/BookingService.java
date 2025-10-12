package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.request.BookingRequest;
import com.group4.evRentalBE.model.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);
    BookingResponse getBookingById(String id);
    List<BookingResponse> getCustomerBookings();
    List<BookingResponse> getStationBookings(Long stationId);
    BookingResponse cancelBooking(String bookingId);
    // Availability checking
    Boolean checkVehicleTypeAvailability(Long stationId, Long typeId, String startDate, String endDate);
    Integer getAvailableVehicleCount(Long stationId, Long typeId, String startDate, String endDate);
}
