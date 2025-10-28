package com.group4.evRentalBE.presentation.controller;

import com.group4.evRentalBE.infrastructure.constant.ResponseObject;
import com.group4.evRentalBE.business.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}