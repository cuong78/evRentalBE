package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.constant.ResponseObject;
import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.model.entity.Booking;
import com.group4.evRentalBE.model.entity.Payment;
import com.group4.evRentalBE.repository.BookingRepository;
import com.group4.evRentalBE.repository.PaymentRepository;
import com.group4.evRentalBE.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Value("${payment.vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${payment.vnpay.secret-key}")
    private String vnpSecretKey;

    @Value("${payment.vnpay.url}")
    private String vnpUrl;

    @Value("${payment.vnpay.return-url}")
    private String vnpReturnUrl;

    @Value("${payment.vnpay.ip-address}")
    private String vnpIpAddress;

    private static final String VNPAY_SUCCESS_CODE = "00";
    private static final String PAYMENT_CANCELED_CODE = "24";
    private static final String TRANSACTION_FAILED_CODE = "02";
    private static final String PAYMENT_PENDING_CODE = "91";

    @Override
    @Transactional
    public ResponseObject createVnPayUrl(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Validate booking status
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be paid");
        }

        // Check if payment is expired
        if (booking.isPaymentExpired()) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            throw new IllegalStateException("Payment time has expired");
        }

        try {
            String vnpUrl = createVNPayPaymentUrl(booking);
            return new ResponseObject(
                    HttpStatus.OK.value(),
                    "VNPay URL created successfully",
                    Map.of(
                            "vnpayUrl", vnpUrl,
                            "expiryTime", booking.getPaymentExpiryTime(),
                            "remainingMinutes",
                            Duration.between(LocalDateTime.now(), booking.getPaymentExpiryTime())
                                    .toMinutes()));
        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay URL", e);
        }
    }

    private String createVNPayPaymentUrl(Booking booking) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedExpireDate = booking.getPaymentExpiryTime().format(formatter);

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", booking.getId());
        vnpParams.put("vnp_OrderInfo", "Payment for booking: " + booking.getId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Amount", String.valueOf(booking.getTotalPayment().intValue() * 100));
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_CreateDate", LocalDateTime.now().format(formatter));
        vnpParams.put("vnp_IpAddr", vnpIpAddress);
        vnpParams.put("vnp_ExpireDate", formattedExpireDate);

        String signData = buildSignData(vnpParams);
        vnpParams.put("vnp_SecureHash", generateHMAC(vnpSecretKey, signData));

        return buildPaymentUrl(vnpUrl, vnpParams);
    }

    private String buildSignData(Map<String, String> params) throws Exception {
        StringBuilder signData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            signData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append('&');
        }
        return signData.deleteCharAt(signData.length() - 1).toString();
    }

    private String generateHMAC(String secretKey, String signData) throws Exception {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        hmacSha512.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));

        StringBuilder result = new StringBuilder();
        for (byte b : hmacSha512.doFinal(signData.getBytes(StandardCharsets.UTF_8))) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private String buildPaymentUrl(String baseUrl, Map<String, String> params) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(baseUrl).append('?');
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append('&');
        }
        return urlBuilder.deleteCharAt(urlBuilder.length() - 1).toString();
    }

    @Override
    @Transactional
    public Map<String, String> handleVNPayReturn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();

        try {
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef");

            Booking booking = bookingRepository.findById(vnp_TxnRef)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

            // Check payment expiry
            if (LocalDateTime.now().isAfter(booking.getPaymentExpiryTime())) {
                handleExpiredBooking(booking);
                return createErrorResponse("98", "Payment time expired");
            }

            // Process payment result
            if (VNPAY_SUCCESS_CODE.equals(vnp_ResponseCode)) {
                processSuccessfulPayment(booking, vnp_TxnRef);
                response.put("RspCode", VNPAY_SUCCESS_CODE);
                response.put("Message", "Payment successful");
            } else {
                processFailedPayment(booking.getId());
                response.put("RspCode", "99");
                response.put("Message", getVnPayErrorMessage(vnp_ResponseCode));
            }

        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Error processing payment: " + e.getMessage());
        }

        return response;
    }


    @Transactional
    public void processSuccessfulPayment(Booking booking, String transactionId) {

        // Create payment record
        Payment payment = Payment.builder()
                .booking(booking)
                .type(Payment.PaymentType.DEPOSIT)
                .method(Payment.PaymentMethod.VNPAY)
                .status(Payment.PaymentStatus.SUCCESS)
                .amount(booking.getTotalPayment())
                .transactionId(transactionId)
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // Update booking status to CONFIRMED
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }


    @Transactional
    public void processFailedPayment(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Create failed payment record
        Payment payment = Payment.builder()
                .booking(booking)
                .type(Payment.PaymentType.DEPOSIT)
                .method(Payment.PaymentMethod.VNPAY)
                .status(Payment.PaymentStatus.FAILED)
                .amount(booking.getTotalPayment())
                .build();

        paymentRepository.save(payment);

        // Cancel booking if payment failed
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private void handleExpiredBooking(Booking booking) {
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private Map<String, String> createErrorResponse(String code, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("RspCode", code);
        response.put("Message", message);
        return response;
    }

    private String getVnPayErrorMessage(String responseCode) {
        switch (responseCode) {
            case PAYMENT_CANCELED_CODE:
                return "Payment canceled by user";
            case TRANSACTION_FAILED_CODE:
                return "Transaction failed";
            case PAYMENT_PENDING_CODE:
                return "Payment pending";
            default:
                return "Payment failed with error code: " + responseCode;
        }
    }
}