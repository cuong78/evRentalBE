package com.group4.evRentalBE.business.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String type;
    private String status;
    private Double amount;
    private LocalDateTime paymentDate;
}
