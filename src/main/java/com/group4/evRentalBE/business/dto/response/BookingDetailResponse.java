package com.group4.evRentalBE.business.dto.response;

import com.group4.evRentalBE.domain.entity.Booking;
import com.group4.evRentalBE.domain.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
    // ===== BOOKING INFORMATION (Always included) =====
    private String id;
    
    // User information
    private Long userId;
    private String userPhone;
    private String username;
    
    // Station information
    private Long stationId;
    private String stationName;
    private String stationAddress;
    
    // Vehicle Type information
    private Long typeId;
    private String typeName;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPayment;
    private Booking.BookingStatus status;
    private Payment.PaymentMethod paymentMethod;
    private LocalDateTime paymentExpiryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // ===== PAYMENT INFORMATION (Included for CONFIRMED, ACTIVE, COMPLETED) =====
    private List<PaymentInfo> payments;
    
    // ===== CONTRACT INFORMATION (Included for ACTIVE, COMPLETED) =====
    private ContractInfo contract;
    
    // ===== RETURN TRANSACTION INFORMATION (Included for COMPLETED only) =====
    private ReturnTransactionInfo returnTransaction;
    
    // ===== NESTED DTOs =====
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private Long id;
        private Payment.PaymentType type;
        private Payment.PaymentMethod method;
        private Payment.PaymentStatus status;
        private Double amount;
        private String transactionId;
        private String description;
        private LocalDateTime paymentDate;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractInfo {
        private Long id;
        private Long vehicleId;
        private String vehicleName;
        private String vehicleStatus;
        private Long documentId;
        private String documentType;
        private String documentNumber;
        private String conditionNotes;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnTransactionInfo {
        private Long id;
        private LocalDateTime returnDate;
        private Double additionalFees;
        private Double refundAmount;
        private String conditionNotes;
        private String photos;
        private Boolean isLateReturn;
        private Long overdueDays;
        private LocalDateTime createdAt;
    }
}
