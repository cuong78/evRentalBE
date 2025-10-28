package com.group4.evRentalBE.business.dto.response;

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
    private Long documentId;
    private String conditionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
