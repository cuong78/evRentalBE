package com.group4.evRentalBE.model.entity;


import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Contract")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ COMPOSITION: Contract thuộc về Booking
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    // ✅ ASSOCIATION: Contract gán cho Vehicle cụ thể
    @OneToOne
    @JoinColumn(name = "vehicle_id", nullable = false, unique = true)
    private Vehicle vehicle;

    @Column(nullable = false)
    private String cccd;

    @Column(columnDefinition = "TEXT")
    private String conditionNotes;

    @Column(columnDefinition = "TEXT")
    private String invoiceDetails;

    private String signaturePhoto;
    private String vehiclePhoto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public boolean validateContract() {
        return cccd != null && !cccd.trim().isEmpty()
                && signaturePhoto != null && !signaturePhoto.trim().isEmpty()
                && vehiclePhoto != null && !vehiclePhoto.trim().isEmpty();
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