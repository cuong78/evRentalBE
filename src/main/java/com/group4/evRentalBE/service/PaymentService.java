package com.group4.evRentalBE.service;

import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.model.entity.Payment;

import java.util.Map;

public interface PaymentService {

    ResponseObject createVnPayUrl(String email);

    Map<String, String> handleVNPayReturn(Map<String, String> params);

    /**
     * Process a refund through VNPay
     * @param originalPayment The original payment to be refunded
     * @param refundAmount The amount to refund
     * @param description Description of the refund
     * @return Map containing the response from VNPay
     */
    Map<String, String> processVnPayRefund(Payment originalPayment, Double refundAmount, String description);

    /**
     * Process payment through customer's wallet
     * @param bookingId The booking ID to pay for
     * @return ResponseObject containing the payment result
     */
    ResponseObject payWithWallet(String bookingId);

}
