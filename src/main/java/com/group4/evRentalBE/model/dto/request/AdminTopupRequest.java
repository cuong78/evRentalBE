package com.group4.evRentalBE.model.dto.request;

import lombok.Data;

@Data
public class AdminTopupRequest {
    private Long userId;
    private Long amount;
    private String note;
}
