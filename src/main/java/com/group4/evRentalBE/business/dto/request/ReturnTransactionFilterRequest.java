package com.group4.evRentalBE.business.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReturnTransactionFilterRequest {
    private Long stationId;
    private Long vehicleTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
}
