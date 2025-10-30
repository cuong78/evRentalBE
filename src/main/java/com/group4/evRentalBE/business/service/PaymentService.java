package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.response.PaymentResponse;
import com.group4.evRentalBE.infrastructure.constant.ResponseObject;
import com.group4.evRentalBE.domain.entity.Payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    List<PaymentResponse> getAllPayments();
    List<PaymentResponse> getPaymentsByStationAndType(Long stationId, Long typeId);
    public List<PaymentResponse> getPaymentsFiltered(Long stationId, Long vehicleTypeId,
                                                     LocalDate startDate, LocalDate endDate);
}
