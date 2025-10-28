package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.model.dto.request.BookingRequest;
import com.group4.evRentalBE.model.dto.response.BookingResponse;
import com.group4.evRentalBE.model.dto.response.BookingDetailResponse;
import com.group4.evRentalBE.model.dto.response.CancelBookingResponse;
import com.group4.evRentalBE.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")

public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest bookingRequest) {
        BookingResponse response = bookingService.createBooking(bookingRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "Get detailed booking information", 
               description = "Returns booking details with related information based on status: " +
                           "PENDING/CANCELLED: Basic booking info only | " +
                           "CONFIRMED: Booking + Payment info | " +
                           "ACTIVE: Booking + Payment + Contract info | " +
                           "COMPLETED: Booking + Payment + Contract + Return Transaction info")
    public ResponseEntity<BookingDetailResponse> getBookingDetail(@PathVariable String id) {
        BookingDetailResponse response = bookingService.getBookingDetailById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        List<BookingResponse> responses = bookingService.getBookingsByCustomer(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/phone/{phone}/confirmed")
    public ResponseEntity<List<BookingResponse>> getConfirmedBookingsByPhone(
            @PathVariable String phone) {

        List<BookingResponse> responses = bookingService.getConfirmedBookingsByPhone(phone);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/phone/{phone}/active")
    public ResponseEntity<List<BookingResponse>> getActiveBookingsByPhone(
            @PathVariable String phone) {

        List<BookingResponse> responses = bookingService.getActiveBookingsByPhone(phone);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(
            @PathVariable String status) {

        List<BookingResponse> responses = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking", 
               description = "Cancel a booking. Rules: " +
                           "1. Can only cancel PENDING or CONFIRMED bookings | " +
                           "2. PENDING: No refund (no payment made yet) | " +
                           "3. CONFIRMED - Cancel >24h before pickup: 100% refund | " +
                           "4. CONFIRMED - Cancel <24h before pickup: 50% refund | " +
                           "5. CONFIRMED - Cancel after pickup time: No refund")
    public ResponseEntity<CancelBookingResponse> cancelBooking(@PathVariable("id") String bookingId) {
        CancelBookingResponse response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }


}