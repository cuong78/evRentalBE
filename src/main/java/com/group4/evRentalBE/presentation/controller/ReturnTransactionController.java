package com.group4.evRentalBE.presentation.controller;

import com.group4.evRentalBE.business.dto.request.ReturnTransactionRequest;
import com.group4.evRentalBE.business.dto.response.ReturnTransactionResponse;
import com.group4.evRentalBE.business.service.ReturnTransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/return-transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")

public class ReturnTransactionController {

    private final ReturnTransactionService returnTransactionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ReturnTransactionResponse> createReturnTransaction(
            @Valid @ModelAttribute ReturnTransactionRequest returnTransactionRequest) {
        ReturnTransactionResponse response = returnTransactionService.createReturnTransaction(returnTransactionRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ReturnTransactionResponse> getReturnTransactionByBooking(
            @PathVariable String bookingId) {
        ReturnTransactionResponse response = returnTransactionService.getReturnTransactionByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }
}