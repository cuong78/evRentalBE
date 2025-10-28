package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.infrastructure.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.business.mapper.DocumentMapper;
import com.group4.evRentalBE.business.dto.request.DocumentRequest;
import com.group4.evRentalBE.business.dto.response.DocumentResponse;
import com.group4.evRentalBE.domain.entity.Document;
import com.group4.evRentalBE.domain.entity.User;
import com.group4.evRentalBE.domain.repository.DocumentRepository;
import com.group4.evRentalBE.domain.repository.UserRepository;
import com.group4.evRentalBE.business.service.DocumentService;
import com.group4.evRentalBE.business.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentMapper documentMapper;
    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public DocumentResponse createDocument(DocumentRequest documentRequest) {
        // Find the user
        User user = userRepository.findById(documentRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Upload photos to cloud
        String frontPhotoUrl = null;
        String backPhotoUrl = null;

        if (documentRequest.getFrontPhoto() != null && !documentRequest.getFrontPhoto().isEmpty()) {
            frontPhotoUrl = fileUploadService.uploadFile(
                    documentRequest.getFrontPhoto(), 
                    "documents"
            );
            log.info("Front photo uploaded: {}", frontPhotoUrl);
        }

        if (documentRequest.getBackPhoto() != null && !documentRequest.getBackPhoto().isEmpty()) {
            backPhotoUrl = fileUploadService.uploadFile(
                    documentRequest.getBackPhoto(), 
                    "documents"
            );
            log.info("Back photo uploaded: {}", backPhotoUrl);
        }

        // If this is set as default, unset any other default documents for this user
        if (documentRequest.isDefault()) {
            Document defaultDocument = documentRepository.findByUserAndIsDefaultTrue(user);
            if (defaultDocument != null) {
                defaultDocument.unsetDefault();
                documentRepository.save(defaultDocument);
            }
        }

        // Create the document with uploaded URLs
        Document document = Document.builder()
                .user(user)
                .documentType(documentRequest.getDocumentType())
                .documentNumber(documentRequest.getDocumentNumber())
                .frontPhoto(frontPhotoUrl)
                .backPhoto(backPhotoUrl)
                .issueDate(documentRequest.getIssueDate())
                .expiryDate(documentRequest.getExpiryDate())
                .issuedBy(documentRequest.getIssuedBy())
                .isDefault(documentRequest.isDefault())
                .status(Document.DocumentStatus.VERIFIED)
                .build();

        // Save the document
        Document savedDocument = documentRepository.save(document);
        log.info("Document created with ID: {}", savedDocument.getId());

        // Return the response
        return documentMapper.toResponse(savedDocument);
    }

    @Override
    public DocumentResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return documentMapper.toResponse(document);
    }

    @Override
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentResponse> getDocumentsByUserId(Long userId) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Find all documents for this user
        return documentRepository.findByUserUserId(userId).stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentRequest documentRequest) {
        // Find the document
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Update the document
        documentMapper.updateEntity(document, documentRequest);

        // If this is set as default, unset any other default documents for this user
        if (document.isDefault()) {
            Document defaultDocument = documentRepository.findByUserAndIsDefaultTrue(document.getUser());
            if (defaultDocument != null && !defaultDocument.getId().equals(id)) {
                defaultDocument.unsetDefault();
                documentRepository.save(defaultDocument);
            }
        }

        // Save the document
        Document updatedDocument = documentRepository.save(document);

        // Return the response
        return documentMapper.toResponse(updatedDocument);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        // Find the document
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Delete photos from cloud
        if (document.getFrontPhoto() != null) {
            fileUploadService.deleteFile(document.getFrontPhoto());
            log.info("Front photo deleted from cloud");
        }
        if (document.getBackPhoto() != null) {
            fileUploadService.deleteFile(document.getBackPhoto());
            log.info("Back photo deleted from cloud");
        }

        // Delete the document
        documentRepository.delete(document);
        log.info("Document deleted with ID: {}", id);
    }
}
