package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.model.dto.request.BookingRequest;
import com.group4.evRentalBE.model.dto.response.BookingResponse;
import com.group4.evRentalBE.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ResponseObject> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        try {
            BookingResponse response = bookingService.createBooking(bookingRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.CREATED.value())
                            .message("Booking created successfully")
                            .data(response)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getBookingById(@PathVariable String id) {
        try {
            BookingResponse booking = bookingService.getBookingById(id);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Booking retrieved successfully")
                            .data(booking)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ResponseObject> getCustomerBookings() {
        try {
            List<BookingResponse> bookings = bookingService.getCustomerBookings();
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Customer bookings retrieved successfully")
                            .data(bookings)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve bookings: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<ResponseObject> getStationBookings(@PathVariable Long stationId) {
        try {
            List<BookingResponse> bookings = bookingService.getStationBookings(stationId);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Station bookings retrieved successfully")
                            .data(bookings)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }



    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ResponseObject> cancelBooking(@PathVariable String bookingId) {
        try {
            BookingResponse response = bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok()
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Booking cancelled successfully")
                            .data(response)
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

}