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


    public enum PaymentType {
        DEPOSIT,           // Tiền cọc khi đặt xe
        REFUND            // Hoàn tiền cọc
    }

    public enum PaymentStatus {
        PENDING,          // Chờ xử lý
        PROCESSING,       // Đang xử lý
        SUCCESS,          // Thành công
        FAILED,           // Thất bại
    }

    public enum PaymentMethod {
        CASH,             // Tiền mặt
        VNPAY,            // VNPay
        WALLET            // Ví điện tử
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