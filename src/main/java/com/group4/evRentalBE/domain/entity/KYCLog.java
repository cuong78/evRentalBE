package com.group4.evRentalBE.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KYCLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "cccd_id", length = 20)
    private String cccdId;
    
    @Column(name = "full_name", length = 255)
    private String fullName;
    
    @Column(name = "date_of_birth", length = 20)
    private String dateOfBirth;
    
    @Column(name = "gender", length = 10)
    private String gender;
    
    @Column(name = "nationality", length = 50)
    private String nationality;
    
    @Column(name = "place_of_origin", length = 255)
    private String placeOfOrigin;
    
    @Column(name = "place_of_residence", length = 255)
    private String placeOfResidence;
    
    @Column(name = "issue_date", length = 20)
    private String issueDate;
    
    @Column(name = "issued_by", length = 255)
    private String issuedBy;
    
    @Column(name = "expiry_date", length = 20)
    private String expiryDate;
    
    @Column(name = "front_image_url", length = 500)
    private String frontImageUrl;
    
    @Column(name = "back_image_url", length = 500)
    private String backImageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20)
    private VerificationStatus verificationStatus;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.verificationStatus == null) {
            this.verificationStatus = VerificationStatus.PENDING;
        }
    }
    
    public enum VerificationStatus {
        PENDING,    // Chờ xác thực
        APPROVED,   // Đã duyệt
        REJECTED    // Từ chối
    }
}
