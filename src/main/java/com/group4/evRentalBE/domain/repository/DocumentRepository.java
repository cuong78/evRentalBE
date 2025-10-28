package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.Document;
import com.group4.evRentalBE.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Find all documents by user
     * @param user the user
     * @return the list of documents
     */
    List<Document> findByUser(User user);

    /**
     * Find all documents by user ID
     * @param userId the user ID
     * @return the list of documents
     */
    List<Document> findByUserUserId(Long userId);

    /**
     * Find all default documents
     * @return the list of documents
     */
    List<Document> findByIsDefaultTrue();

    /**
     * Find default document by user
     * @param user the user
     * @return the document
     */
    Document findByUserAndIsDefaultTrue(User user);
}
