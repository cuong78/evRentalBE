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
        // Validate user exists
        User user = userRepository.findById(documentRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check for duplicate document number
        if (documentRepository.existsByDocumentNumber(documentRequest.getDocumentNumber())) {
            throw new ConflictException("Document with this number already exists");
        }

        // If setting as default, ensure no other default exists for this user
        if (documentRequest.isDefault()) {
            int defaultCount = documentRepository.countDefaultDocumentsByUserId(user.getUserId());
            if (defaultCount > 0) {
                throw new ConflictException("User already has a default document. Please unset the current default first.");
            }
        }

        Document document = documentMapper.toEntity(documentRequest, user);
        Document savedDocument = documentRepository.save(document);

        return documentMapper.toResponse(savedDocument);
    }

    @Override
    public DocumentResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        return documentMapper.toResponse(document);
    }

    @Override
    public List<DocumentResponse> getDocumentsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return documentRepository.findByUserUserId(userId)
                .stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentResponse getDefaultDocumentByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        Document document = documentRepository.findByUserUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No default document found for user"));
        
        return documentMapper.toResponse(document);
    }

    @Override
    public List<DocumentResponse> getValidDocumentsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return documentRepository.findValidDocumentsByUserId(userId, LocalDateTime.now())
                .stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentRequest documentRequest) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Check for duplicate document number if changed
        if (!document.getDocumentNumber().equals(documentRequest.getDocumentNumber()) &&
            documentRepository.existsByDocumentNumber(documentRequest.getDocumentNumber())) {
            throw new ConflictException("Document with this number already exists");
        }

        documentMapper.updateEntity(document, documentRequest);
        Document savedDocument = documentRepository.save(document);

        return documentMapper.toResponse(savedDocument);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        documentRepository.delete(document);
    }

    @Override
    @Transactional
    public DocumentResponse verifyDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        document.verify();
        Document savedDocument = documentRepository.save(document);

        return documentMapper.toResponse(savedDocument);
    }

    @Override
    @Transactional
    public DocumentResponse rejectDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        document.reject();
        Document savedDocument = documentRepository.save(document);

        return documentMapper.toResponse(savedDocument);
    }

    @Override
    @Transactional
    public DocumentResponse setAsDefault(Long userId, Long documentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (!document.getUser().getUserId().equals(userId)) {
            throw new ConflictException("Document does not belong to the specified user");
        }

        if (!document.isValid()) {
            throw new ConflictException("Cannot set invalid document as default");
        }

        // Unset current default
        user.getDocuments().forEach(Document::unsetDefault);
        documentRepository.saveAll(user.getDocuments());

        // Set new default
        document.setAsDefault();
        Document savedDocument = documentRepository.save(document);

        return documentMapper.toResponse(savedDocument);
    }

    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void expireOldDocuments() {
        List<Document> expiredDocuments = documentRepository.findExpiredDocuments(LocalDateTime.now());
        
        for (Document document : expiredDocuments) {
            document.setStatus(Document.DocumentStatus.EXPIRED);
        }
        
        documentRepository.saveAll(expiredDocuments);
    }
}