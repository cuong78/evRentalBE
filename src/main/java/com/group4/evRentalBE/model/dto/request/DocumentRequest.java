package com.group4.evRentalBE.model.dto.request;

import com.group4.evRentalBE.model.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Document type is required")
    private Document.DocumentType documentType;
    
    @NotBlank(message = "Document number is required")
    private String documentNumber;
    
    // Changed to MultipartFile for file upload
    private MultipartFile frontPhoto;
    private MultipartFile backPhoto;
    
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuedBy;
    private boolean isDefault = false;
}