package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.model.dto.request.ReturnTransactionRequest;
import com.group4.evRentalBE.model.dto.response.ReturnTransactionResponse;
import com.group4.evRentalBE.service.ReturnTransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/return-transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")

public class ReturnTransactionController {

    private final ReturnTransactionService returnTransactionService;

    @PostMapping
    public ResponseEntity<ReturnTransactionResponse> createReturnTransaction(
            @RequestBody ReturnTransactionRequest returnTransactionRequest) {
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