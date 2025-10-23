package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.model.dto.request.BookingRequest;
import com.group4.evRentalBE.model.dto.response.BookingResponse;
import com.group4.evRentalBE.service.BookingService;
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


}