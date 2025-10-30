package com.group4.evRentalBE.business.service.impl;

import com.group4.evRentalBE.business.dto.response.CCCDInfo;
import com.group4.evRentalBE.domain.entity.KYCLog;
import com.group4.evRentalBE.domain.entity.User;
import com.group4.evRentalBE.domain.repository.KYCLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KYCService {
    
    private final KYCLogRepository kycLogRepository;
    
    @Transactional
    public KYCLog saveKYCLog(User user, CCCDInfo cccdInfo, String frontImageUrl, String backImageUrl) {
        KYCLog kycLog = KYCLog.builder()
                .user(user)
                .cccdId(cccdInfo.getId())
                .fullName(cccdInfo.getFullName())
                .dateOfBirth(cccdInfo.getDateOfBirth())
                .gender(cccdInfo.getGender())
                .nationality(cccdInfo.getNationality())
                .placeOfOrigin(cccdInfo.getPlaceOfOrigin())
                .placeOfResidence(cccdInfo.getPlaceOfResidence())
                .issueDate(cccdInfo.getIssueDate())
                .issuedBy(cccdInfo.getIssuedBy())
                .expiryDate(cccdInfo.getExpiryDate())
                .frontImageUrl(frontImageUrl)
                .backImageUrl(backImageUrl)
                .verificationStatus(KYCLog.VerificationStatus.PENDING)
                .build();
        
        KYCLog saved = kycLogRepository.save(kycLog);
        log.info("Saved KYC log for user with CCCD ID: {}", cccdInfo.getId());
        
        return saved;
    }
    
    public List<KYCLog> getUserKYCHistory(User user) {
        return kycLogRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public KYCLog getLatestKYCLog(User user) {
        return kycLogRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .orElse(null);
    }
    
    @Transactional
    public KYCLog updateVerificationStatus(Long kycLogId, KYCLog.VerificationStatus status, String notes) {
        KYCLog kycLog = kycLogRepository.findById(kycLogId)
                .orElseThrow(() -> new IllegalArgumentException("KYC log not found"));
        
        kycLog.setVerificationStatus(status);
        kycLog.setNotes(notes);
        
        if (status == KYCLog.VerificationStatus.APPROVED) {
            kycLog.setVerifiedAt(java.time.LocalDateTime.now());
        }
        
        return kycLogRepository.save(kycLog);
    }
    
    public boolean hasApprovedKYC(User user) {
        return kycLogRepository.existsByUserAndVerificationStatus(
            user, 
            KYCLog.VerificationStatus.APPROVED
        );
    }
    
    public List<KYCLog> getPendingKYCLogs() {
        return kycLogRepository.findByVerificationStatus(KYCLog.VerificationStatus.PENDING);
    }
}
