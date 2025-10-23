package com.group4.evRentalBE.model.entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Column(name = "front_photo")
    private String frontPhoto;

    @Column(name = "back_photo")
    private String backPhoto;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issued_by")
    private String issuedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ ENUMS
    public enum DocumentType {
        CMND,
        CCCD,
        PASSPORT,
        DRIVING_LICENSE
    }

    public enum DocumentStatus {
        PENDING,
        VERIFIED,
        EXPIRED,
        REJECTED
    }

    public boolean isValid() {
        return status == DocumentStatus.VERIFIED 
                && (expiryDate == null || expiryDate.isAfter(LocalDate.now()));
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetDefault() {
        this.isDefault = false;
    }

    public void verify() {
        this.status = DocumentStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = DocumentStatus.REJECTED;
        this.verifiedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Auto-expire if expiry date is past
        if (isExpired()) {
            status = DocumentStatus.EXPIRED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // Auto-expire if expiry date is past
        if (isExpired() && status == DocumentStatus.VERIFIED) {
            status = DocumentStatus.EXPIRED;
        }
    }
}