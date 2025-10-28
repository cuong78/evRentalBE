package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.BookingRequest;
import com.group4.evRentalBE.business.dto.response.BookingResponse;
import com.group4.evRentalBE.business.dto.response.BookingDetailResponse;
import com.group4.evRentalBE.business.dto.response.CancelBookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);
    BookingResponse getBookingById(String id);
    BookingDetailResponse getBookingDetailById(String id);
    List<BookingResponse> getBookingsByCustomer(Long userId);
    void cancelExpiredBookings();
    List<BookingResponse> getConfirmedBookingsByPhone(String phone);
    List<BookingResponse> getActiveBookingsByPhone(String phone);
    List<BookingResponse> getBookingsByStatus(String status);
    CancelBookingResponse cancelBooking(String bookingId);
}
