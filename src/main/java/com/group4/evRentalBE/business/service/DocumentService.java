package com.group4.evRentalBE.business.service;

import com.group4.evRentalBE.business.dto.request.DocumentRequest;
import com.group4.evRentalBE.business.dto.response.DocumentResponse;

import java.util.List;

public interface DocumentService {

    /**
     * Create a new document
     * @param documentRequest the document request
     * @return the created document response
     */
    DocumentResponse createDocument(DocumentRequest documentRequest);

    /**
     * Get a document by ID
     * @param id the document ID
     * @return the document response
     */
    DocumentResponse getDocumentById(Long id);

    /**
     * Get all documents
     * @return the list of document responses
     */
    List<DocumentResponse> getAllDocuments();

    /**
     * Get all documents by user ID
     * @param userId the user ID
     * @return the list of document responses
     */
    List<DocumentResponse> getDocumentsByUserId(Long userId);

    /**
     * Update a document
     * @param id the document ID
     * @param documentRequest the document request
     * @return the updated document response
     */
    DocumentResponse updateDocument(Long id, DocumentRequest documentRequest);

    /**
     * Delete a document
     * @param id the document ID
     */
    void deleteDocument(Long id);
}
