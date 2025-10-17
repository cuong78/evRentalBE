package com.group4.evRentalBE.service.impl;

import com.group4.evRentalBE.exception.exceptions.ConflictException;
import com.group4.evRentalBE.exception.exceptions.ResourceNotFoundException;
import com.group4.evRentalBE.mapper.DocumentMapper;
import com.group4.evRentalBE.model.dto.request.DocumentRequest;
import com.group4.evRentalBE.model.dto.response.DocumentResponse;
import com.group4.evRentalBE.model.entity.Document;
import com.group4.evRentalBE.model.entity.User;
import com.group4.evRentalBE.repository.DocumentRepository;
import com.group4.evRentalBE.repository.UserRepository;
import com.group4.evRentalBE.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public DocumentResponse createDocument(DocumentRequest documentRequest) {
        // Find the user
        User user = userRepository.findById(documentRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create the document
        Document document = documentMapper.toEntity(documentRequest, user);

        // If this is set as default, unset any other default documents for this user
        if (document.isDefault()) {
            Document defaultDocument = documentRepository.findByUserAndIsDefaultTrue(user);
            if (defaultDocument != null) {
                defaultDocument.unsetDefault();
                documentRepository.save(defaultDocument);
            }
        }

        // Save the document
        Document savedDocument = documentRepository.save(document);

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

        // Delete the document
        documentRepository.delete(document);
    }
}
