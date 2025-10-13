package com.group4.evRentalBE.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {
    private Long id;
    private String bookingId;
    private Long vehicleId;
    private String cccd;
    private String conditionNotes;
    private String invoiceDetails;
    private String signaturePhoto;
    private String vehiclePhoto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for frontend display
    private String customerName;
    private String vehicleName;
    private String stationName;
    private LocalDateTime bookingStartDate;
    private LocalDateTime bookingEndDate;
}