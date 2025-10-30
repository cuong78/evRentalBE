package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.KYCLog;
import com.group4.evRentalBE.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KYCLogRepository extends JpaRepository<KYCLog, Long> {
    
    List<KYCLog> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<KYCLog> findFirstByUserOrderByCreatedAtDesc(User user);
    
    List<KYCLog> findByVerificationStatus(KYCLog.VerificationStatus status);
    
    @Query("SELECT k FROM KYCLog k WHERE k.cccdId = :cccdId ORDER BY k.createdAt DESC")
    List<KYCLog> findByCccdId(@Param("cccdId") String cccdId);
    
    boolean existsByUserAndVerificationStatus(User user, KYCLog.VerificationStatus status);
}
