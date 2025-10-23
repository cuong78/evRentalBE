package com.group4.evRentalBE.model.dto.response;

import com.group4.evRentalBE.model.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private Document.DocumentType documentType;
    private String documentNumber;
    private String frontPhoto;
    private String backPhoto;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuedBy;
    private Document.DocumentStatus status;
    private LocalDateTime verifiedAt;
    private boolean isDefault;
    private boolean isValid;
    private boolean isExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}