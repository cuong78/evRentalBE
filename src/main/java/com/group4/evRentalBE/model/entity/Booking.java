package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private RentalStation station;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private VehicleType type;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Payment.PaymentMethod paymentMethod;  // Phương thức thanh toán mặc định

    @Column(nullable = false)
    private Double depositPaid = 0.0;

    @Column(nullable = false)
    private Double rentalFee;

    @Column(nullable = false)
    private Double totalInitialPayment = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ COMPOSITION: Booking owns Contract
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contract contract;

    // ✅ COMPOSITION: Booking owns ReturnTransaction
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReturnTransaction returnTransaction;

    // ✅ COMPOSITION: Booking owns Payments
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffBooking> staffBookings = new ArrayList<>();

    // ✅ BUSINESS METHODS
    public Double calculateTotalCost() {
        long days = getRentalDays();
        return days * rentalFee;
    }

    public long getRentalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == BookingStatus.ACTIVE
                && now.isAfter(startDate)
                && now.isBefore(endDate)
                && returnTransaction == null;
    }

    public boolean canCancel() {
        return LocalDateTime.now().isBefore(startDate.minusHours(24))
                && (status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED)
                && contract == null;
    }


    // ✅ PAYMENT RELATED METHODS
    public Double getTotalPaid() {
        return payments.stream()
                .filter(Payment::isSuccessful)
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public Double getRemainingAmount() {
        return Math.max(0, calculateTotalCost() - getTotalPaid());
    }

    public boolean isFullyPaid() {
        return getTotalPaid() >= calculateTotalCost();
    }



    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setBooking(this);

        // Cập nhật depositPaid nếu là payment DEPOSIT
        if (payment.getType() == Payment.PaymentType.DEPOSIT && payment.isSuccessful()) {
            this.depositPaid += payment.getAmount();
        }
    }



    public void cancel() {
        if (!canCancel()) {
            throw new IllegalStateException("Booking cannot be cancelled");
        }
        this.status = BookingStatus.CANCELLED;
    }

    public enum BookingStatus {
        PENDING,      // Chờ xác nhận (vừa đặt)
        CONFIRMED,    // Đã xác nhận (đã thanh toán cọc)
        ACTIVE,       // Đang thuê (đã nhận xe)
        COMPLETED,    // Hoàn thành (đã trả xe)
        CANCELLED     // Đã hủy
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}