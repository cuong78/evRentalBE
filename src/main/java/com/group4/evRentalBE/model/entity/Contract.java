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

    // ✅ BUSINESS METHODS
    public String generateInvoice() {
        StringBuilder invoice = new StringBuilder();
        invoice.append("========== HÓA ĐƠN THUÊ XE ==========\n");
        invoice.append("Mã booking: ").append(booking.getId()).append("\n");
        invoice.append("Khách hàng: ").append(booking.getCustomer().getUser().getUsername()).append("\n");
        invoice.append("CCCD: ").append(cccd).append("\n");
        invoice.append("Loại xe: ").append(vehicle.getType().getName()).append("\n");
        invoice.append("Biển số xe: ").append(vehicle.getId()).append("\n");
        invoice.append("Điểm thuê: ").append(booking.getStation().getAddress()).append("\n");
        invoice.append("Thời gian: ").append(booking.getStartDate())
                .append(" đến ").append(booking.getEndDate()).append("\n");
        invoice.append("Số ngày thuê: ").append(booking.getRentalDays()).append(" ngày\n");
        invoice.append("Phí thuê: ").append(booking.getRentalFee()).append(" VNĐ/ngày\n");
        invoice.append("Tổng phí thuê: ").append(booking.calculateTotalCost()).append(" VNĐ\n");
        invoice.append("Đặt cọc: ").append(booking.getDepositPaid()).append(" VNĐ\n");
        invoice.append("Tổng thanh toán: ").append(booking.getTotalInitialPayment()).append(" VNĐ\n");
        invoice.append("=====================================\n");

        this.invoiceDetails = invoice.toString();
        return this.invoiceDetails;
    }

    public void signContract(String signaturePath) {
        if (signaturePath == null || signaturePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Signature path is required");
        }
        this.signaturePhoto = signaturePath;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean validateContract() {
        return cccd != null && !cccd.trim().isEmpty()
                && signaturePhoto != null && !signaturePhoto.trim().isEmpty()
                && vehiclePhoto != null && !vehiclePhoto.trim().isEmpty();
    }

    public boolean isComplete() {
        return validateContract() && invoiceDetails != null;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (invoiceDetails == null) {
            generateInvoice();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}