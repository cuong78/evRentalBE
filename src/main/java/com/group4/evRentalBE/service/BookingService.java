package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.request.BookingRequest;
import com.group4.evRentalBE.model.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);
    BookingResponse getBookingById(String id);
    List<BookingResponse> getBookingsByCustomer(Long userId);
    void cancelExpiredBookings();
    List<BookingResponse> getConfirmedBookingsByPhone(String phone);

    List<BookingResponse> getActiveBookingsByPhone(String phone);
}
