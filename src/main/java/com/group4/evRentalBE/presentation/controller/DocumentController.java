package com.group4.evRentalBE.presentation.controller;

import com.group4.evRentalBE.business.dto.request.DocumentRequest;
import com.group4.evRentalBE.business.dto.response.DocumentResponse;
import com.group4.evRentalBE.business.service.DocumentService;
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


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @ModelAttribute DocumentRequest documentRequest) {
        DocumentResponse response = documentService.createDocument(documentRequest);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<DocumentResponse> responses = documentService.getAllDocuments();
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByUserId(@PathVariable Long userId) {
        List<DocumentResponse> responses = documentService.getDocumentsByUserId(userId);
        return ResponseEntity.ok(responses);
    }


    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(@PathVariable Long id, @RequestBody DocumentRequest documentRequest) {
        DocumentResponse response = documentService.updateDocument(id, documentRequest);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}