package com.group4.evRentalBE.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnTransactionRequest {

    @NotNull(message = "Booking ID is required")
    private String bookingId;

    private String conditionNotes;
    
    // Changed to MultipartFile array for multiple photos
    private MultipartFile[] photos;

    private Double damageFee;
}