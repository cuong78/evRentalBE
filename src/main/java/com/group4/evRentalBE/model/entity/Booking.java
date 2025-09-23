package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Double depositPaid;

    @Column(nullable = false)
    private Double rentalFee;

    @Column(nullable = false)
    private Double totalInitialPayment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Contract contract;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private ReturnTransaction returnTransaction;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<StaffBooking> staffBookings;

    public enum PaymentMethod {
        CASH, TRANSFER, CARD
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