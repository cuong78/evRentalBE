package com.group4.evRentalBE.domain.entity;


import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "VehicleType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Double depositAmount;

    @Column(nullable = false)
    private Double rentalRate;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Integer seats; // Số chỗ ngồi (4, 5, 6, 7)

    @Column(nullable = false)
    private Integer range; // Quãng đường tối đa (km)

    @Column(length = 50)
    private String rangeStandard; // Chuẩn đo (NEDC, WLTP)

    @Column(nullable = false)
    private Integer trunkCapacity; // Dung tích cốp (lít)

    @Column(length = 50)
    private String category; // Phân khúc: Minicar, B-SUV, C-SUV, D-SUV, E-SUV

    @Column(length = 1000)
    private String description; // Mô tả chi tiết

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL)
    private List<Vehicle> vehicles;

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL)
    private List<Booking> bookings;

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
