package com.group4.evRentalBE.controller;

import com.group4.evRentalBE.model.dto.request.DocumentRequest;
import com.group4.evRentalBE.model.dto.response.DocumentResponse;
import com.group4.evRentalBE.service.DocumentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Create a new document with file upload
     * @param documentRequest the document request
     * @return the created document response
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @ModelAttribute DocumentRequest documentRequest) {
        DocumentResponse response = documentService.createDocument(documentRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a document by ID
     * @param id the document ID
     * @return the document response
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all documents
     * @return the list of document responses
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<DocumentResponse> responses = documentService.getAllDocuments();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all documents by user ID
     * @param userId the user ID
     * @return the list of document responses
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByUserId(@PathVariable Long userId) {
        List<DocumentResponse> responses = documentService.getDocumentsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Update a document
     * @param id the document ID
     * @param documentRequest the document request
     * @return the updated document response
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(@PathVariable Long id, @RequestBody DocumentRequest documentRequest) {
        DocumentResponse response = documentService.updateDocument(id, documentRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a document
     * @param id the document ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}