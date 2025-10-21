package com.group4.evRentalBE.model.entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDate;
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private RentalStation station;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private VehicleType type;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Payment.PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Double totalPayment; // Tổng số tiền phải thanh toán (deposit + rental fee)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime paymentExpiryTime; // Thời hạn thanh toán (10 phút)

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contract contract;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReturnTransaction returnTransaction;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    public Double calculateTotalCost() {
        long days = getRentalDays();
        return (days * type.getRentalRate()) + type.getDepositAmount();
    }

    public long getRentalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }



    public boolean isPaymentExpired() {
        return paymentExpiryTime != null && LocalDateTime.now().isAfter(paymentExpiryTime);
    }





    public enum BookingStatus {
        PENDING,      // Chờ thanh toán (10 phút)
        CONFIRMED,    // Đã thanh toán đủ
        ACTIVE,       // Đang thuê (đã nhận xe)
        COMPLETED,    // Hoàn thành (đã trả xe và hoàn tiền)
        CANCELLED,    // Đã hủy
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Set payment expiry time to 10 minutes from creation
        paymentExpiryTime = LocalDateTime.now().plusMinutes(10);
        // Calculate total payment
        totalPayment = calculateTotalCost();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}