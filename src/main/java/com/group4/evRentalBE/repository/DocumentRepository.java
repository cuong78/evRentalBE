package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByUserUserId(Long userId);
    
    Optional<Document> findByUserUserIdAndIsDefaultTrue(Long userId);
    
    List<Document> findByUserUserIdAndStatus(Long userId, Document.DocumentStatus status);
    
    @Query("SELECT d FROM Document d WHERE d.user.userId = :userId AND d.status = 'VERIFIED' AND (d.expiryDate IS NULL OR d.expiryDate > :now)")
    List<Document> findValidDocumentsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT d FROM Document d WHERE d.expiryDate < :now AND d.status = 'VERIFIED'")
    List<Document> findExpiredDocuments(@Param("now") LocalDateTime now);
    
    boolean existsByDocumentNumber(String documentNumber);
    
    boolean existsByUserUserIdAndDocumentTypeAndStatus(Long userId, Document.DocumentType documentType, Document.DocumentStatus status);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.user.userId = :userId AND d.isDefault = true")
    int countDefaultDocumentsByUserId(@Param("userId") Long userId);
}