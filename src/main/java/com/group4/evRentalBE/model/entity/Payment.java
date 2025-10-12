package com.group4.evRentalBE.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ COMPOSITION: Payment thuộc về Booking
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private Double amount;

    // Mã giao dịch từ payment gateway (VNPay, Momo, etc.)
    private String transactionId;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Response từ payment gateway (JSON format)
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ BUSINESS METHODS
    public boolean processPayment() {
        if (this.status == PaymentStatus.SUCCESS) {
            return false; // Đã thanh toán rồi
        }

        this.status = PaymentStatus.PROCESSING;
        this.paymentDate = LocalDateTime.now();
        return true;
    }

    public void confirmPayment() {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Payment must be in PROCESSING state");
        }
        this.status = PaymentStatus.SUCCESS;
        this.paymentDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void failPayment(String reason) {
        this.status = PaymentStatus.FAILED;
        this.description = (this.description != null ? this.description + " | " : "") + "Failed: " + reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelPayment() {
        if (this.status == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Cannot cancel successful payment");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void refundPayment() {
        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Can only refund successful payments");
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isSuccessful() {
        return this.status == PaymentStatus.SUCCESS;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean canBeProcessed() {
        return this.status == PaymentStatus.PENDING || this.status == PaymentStatus.FAILED;
    }

    public String getPaymentInfo() {
        return String.format("Payment #%d - %s: %.2f VND via %s - Status: %s",
                id, type, amount, method, status);
    }

    // ✅ ENUMS
    public enum PaymentType {
        DEPOSIT,           // Tiền cọc khi đặt xe
        RENTAL_FEE,        // Phí thuê xe (nếu thanh toán sau)
        ADDITIONAL_FEE,    // Phí phát sinh (trễ giờ, hư hỏng, etc.)
        REFUND            // Hoàn tiền cọc
    }

    public enum PaymentStatus {
        PENDING,          // Chờ xử lý
        PROCESSING,       // Đang xử lý
        SUCCESS,          // Thành công
        FAILED,           // Thất bại
        CANCELLED,        // Đã hủy
        REFUNDED          // Đã hoàn tiền
    }

    public enum PaymentMethod {
        CASH,             // Tiền mặt
        VNPAY,           // VNPay

    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paymentDate == null && status == PaymentStatus.SUCCESS) {
            paymentDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}