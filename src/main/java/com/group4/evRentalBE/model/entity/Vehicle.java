package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ AGGREGATION: Vehicle thuộc VehicleType (không cascade)
    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private VehicleType type;

    // ✅ AGGREGATION: Vehicle ở RentalStation (không cascade)
    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private RentalStation station;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String conditionNotes;

    private String photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ COMPOSITION: Contract thuộc về Vehicle (nếu có)
    @OneToOne(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contract contract;

    // ✅ BUSINESS METHODS
    public boolean isAvailable() {
        return this.status == VehicleStatus.AVAILABLE;
    }

    public void updateStatus(VehicleStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void rentOut() {
        if (!isAvailable()) {
            throw new IllegalStateException("Vehicle is not available for rent");
        }
        this.status = VehicleStatus.RENTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void returnVehicle() {
        if (this.status != VehicleStatus.RENTED) {
            throw new IllegalStateException("Vehicle is not currently rented");
        }
        this.status = VehicleStatus.AVAILABLE;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDamaged(String notes) {
        this.status = VehicleStatus.DAMAGED;
        this.conditionNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void sendToMaintenance(String notes) {
        this.status = VehicleStatus.MAINTENANCE;
        this.conditionNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public enum VehicleStatus {
        AVAILABLE, RENTED, DAMAGED, MAINTENANCE
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
