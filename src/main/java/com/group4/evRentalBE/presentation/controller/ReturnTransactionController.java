package com.group4.evRentalBE.presentation.controller;

import com.group4.evRentalBE.business.dto.request.ReturnTransactionFilterRequest;
import com.group4.evRentalBE.business.dto.request.ReturnTransactionRequest;
import com.group4.evRentalBE.business.dto.response.ReturnTransactionResponse;
import com.group4.evRentalBE.business.service.ReturnTransactionService;
import com.group4.evRentalBE.infrastructure.constant.ResponseObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping
    public ResponseEntity<ResponseObject> getAllReturnTransactions() {
        try {
            List<ReturnTransactionResponse> transactions = returnTransactionService.getAllReturnTransactions();

            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Return transactions retrieved successfully")
                            .data(transactions)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve return transactions: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @PostMapping("/filter")
    public ResponseEntity<ResponseObject> getReturnTransactionsFiltered(@RequestBody ReturnTransactionFilterRequest request) {
        try {
            List<ReturnTransactionResponse> transactions = returnTransactionService.getReturnTransactionsFiltered(
                    request.getStationId(),
                    request.getVehicleTypeId(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Return transactions filtered successfully")
                            .data(transactions)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ResponseObject.builder()
                            .statusCode(500)
                            .message("Failed to filter return transactions: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }
}