package com.group4.evRentalBE.domain.repository;


import com.group4.evRentalBE.domain.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);

}

