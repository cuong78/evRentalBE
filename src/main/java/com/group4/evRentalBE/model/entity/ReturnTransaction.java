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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
