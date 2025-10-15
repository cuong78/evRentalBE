package com.group4.evRentalBE.service;

import com.group4.evRentalBE.model.dto.request.DocumentRequest;
import com.group4.evRentalBE.model.dto.response.DocumentResponse;

import java.util.List;

public interface DocumentService {
    DocumentResponse createDocument(DocumentRequest documentRequest);
    DocumentResponse getDocumentById(Long id);
    List<DocumentResponse> getDocumentsByUserId(Long userId);
    DocumentResponse getDefaultDocumentByUserId(Long userId);
    List<DocumentResponse> getValidDocumentsByUserId(Long userId);
    DocumentResponse updateDocument(Long id, DocumentRequest documentRequest);
    void deleteDocument(Long id);
    DocumentResponse verifyDocument(Long id);
    DocumentResponse rejectDocument(Long id);
    DocumentResponse setAsDefault(Long userId, Long documentId);
    void expireOldDocuments();
}