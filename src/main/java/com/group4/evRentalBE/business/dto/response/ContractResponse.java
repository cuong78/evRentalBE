package com.group4.evRentalBE.business.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponse {
    private Long id;
    private String bookingId;
    private Long vehicleId;
    private Long stationId;
    private Long vehicleTypeId;
    private Long documentId;
    private String conditionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
