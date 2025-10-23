package com.group4.evRentalBE.mapper;

import com.group4.evRentalBE.model.dto.request.DocumentRequest;
import com.group4.evRentalBE.model.dto.response.DocumentResponse;
import com.group4.evRentalBE.model.entity.Document;
import com.group4.evRentalBE.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(Document document) {
        if (document == null) {
            return null;
        }

        return DocumentResponse.builder()
                .id(document.getId())
                .userId(document.getUser().getUserId())
                .username(document.getUser().getUsername())
                .documentType(document.getDocumentType())
                .documentNumber(document.getDocumentNumber())
                .frontPhoto(document.getFrontPhoto())
                .backPhoto(document.getBackPhoto())
                .issueDate(document.getIssueDate())
                .expiryDate(document.getExpiryDate())
                .issuedBy(document.getIssuedBy())
                .status(document.getStatus())
                .verifiedAt(document.getVerifiedAt())
                .isDefault(document.isDefault())
                .isValid(document.isValid())
                .isExpired(document.isExpired())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }



    public void updateEntity(Document document, DocumentRequest request) {
        document.setDocumentType(request.getDocumentType());
        document.setDocumentNumber(request.getDocumentNumber());
        document.setFrontPhoto(String.valueOf(request.getFrontPhoto()));
        document.setBackPhoto(String.valueOf(request.getBackPhoto()));
        document.setIssueDate(request.getIssueDate());
        document.setExpiryDate(request.getExpiryDate());
        document.setIssuedBy(request.getIssuedBy());
    }
}