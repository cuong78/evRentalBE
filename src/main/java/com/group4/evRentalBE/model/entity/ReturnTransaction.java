package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ReturnTransaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false)
    private LocalDateTime returnDate;

    @Column(columnDefinition = "double precision DEFAULT 0")
    private Double additionalFees = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundMethod refundMethod;

    @Column(nullable = false)
    private Double refundAmount = 0.0;

    @Column(columnDefinition = "TEXT")
    private String conditionNotes;

    private String photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "returnTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffReturn> staffReturns = new ArrayList<>();

    // ✅ BUSINESS METHODS
    public Double calculateAdditionalFees() {
        Double fees = 0.0;

        // Phí trễ hạn
        if (isLateReturn()) {
            long overdueDays = getOverdueDays();
            fees += overdueDays * booking.getRentalFee() * 1.5; // 150% phí thường
        }

        // Phí hư hỏng (nếu có ghi chú về hư hỏng)
        if (conditionNotes != null &&
                (conditionNotes.toLowerCase().contains("hư") ||
                        conditionNotes.toLowerCase().contains("damaged"))) {
            fees += 500000.0; // Phí cơ bản cho hư hỏng
        }

        return fees;
    }

    public Double calculateRefund() {
        // Tổng tiền cọc - phí phát sinh
        Double totalDeposit = booking.getDepositPaid();
        return Math.max(0, totalDeposit - additionalFees);
    }

    public void processRefund() {
        this.additionalFees = calculateAdditionalFees();
        this.refundAmount = calculateRefund();
        this.updatedAt = LocalDateTime.now();

        // Tạo Payment record cho refund nếu có tiền hoàn
        if (this.refundAmount > 0) {
            Payment refundPayment = Payment.builder()
                    .booking(this.booking)
                    .type(Payment.PaymentType.REFUND)
                    .method(Payment.PaymentMethod.valueOf(this.refundMethod.name()))
                    .status(Payment.PaymentStatus.SUCCESS)
                    .amount(this.refundAmount)
                    .description("Refund for booking #" + booking.getId())
                    .paymentDate(LocalDateTime.now())
                    .build();
            booking.addPayment(refundPayment);
        }

        // Tạo Payment record cho additional fees nếu có
        if (this.additionalFees > 0) {
            Payment feePayment = Payment.builder()
                    .booking(this.booking)
                    .type(Payment.PaymentType.ADDITIONAL_FEE)
                    .method(booking.getPaymentMethod())
                    .status(Payment.PaymentStatus.PENDING)
                    .amount(this.additionalFees)
                    .description("Additional fees: Late return or damage")
                    .build();
            booking.addPayment(feePayment);
        }
    }

    public boolean isLateReturn() {
        return returnDate.isAfter(booking.getEndDate());
    }

    public long getOverdueDays() {
        if (!isLateReturn()) return 0;
        return ChronoUnit.DAYS.between(booking.getEndDate(), returnDate);
    }

    public enum RefundMethod {
        CASH,      // Hoàn tiền mặt
        TRANSFER   // Hoàn qua chuyển khoản
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        processRefund(); // Tự động tính toán khi tạo
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
