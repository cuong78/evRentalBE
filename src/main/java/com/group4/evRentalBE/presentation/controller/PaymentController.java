package com.group4.evRentalBE.presentation.controller;

import com.group4.evRentalBE.business.dto.request.PaymentFilterRequest;
import com.group4.evRentalBE.business.dto.response.PaymentResponse;
import com.group4.evRentalBE.infrastructure.constant.ResponseObject;
import com.group4.evRentalBE.business.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/vnpay/{bookingId}")
    public ResponseObject createVnPayUrl(@PathVariable String bookingId) {
        return paymentService.createVnPayUrl(bookingId);
    }

    @PostMapping("/wallet/{bookingId}")
    public ResponseObject payWithWallet(@PathVariable String bookingId) {
        return paymentService.payWithWallet(bookingId);
    }

    @GetMapping("/vnpay-return")
    public Map<String, String> handleVNPayReturn(@RequestParam Map<String, String> params) {
        return paymentService.handleVNPayReturn(params);
    }


    @GetMapping
    public ResponseEntity<ResponseObject> getAllPayments() {
        try {
            List<PaymentResponse> payments = paymentService.getAllPayments();

            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Payments retrieved successfully")
                            .data(payments)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to retrieve payments: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }


    @GetMapping("/filter")
    public ResponseEntity<ResponseObject> getPaymentsByStationAndType(
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) Long typeId) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByStationAndType(stationId, typeId);

            return ResponseEntity.ok(
                    ResponseObject.builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Payments filtered successfully")
                            .data(payments)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseObject.builder()
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to filter payments: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @PostMapping("/filter")
    public ResponseEntity<ResponseObject> getPaymentsFiltered(@RequestBody PaymentFilterRequest request) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsFiltered(
                    request.getStationId(),
                    request.getVehicleTypeId(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            return ResponseEntity.ok(ResponseObject.builder()
                    .statusCode(200)
                    .message("Payments filtered successfully")
                    .data(payments)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseObject.builder()
                    .statusCode(500)
                    .message("Failed to filter payments: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }


}
