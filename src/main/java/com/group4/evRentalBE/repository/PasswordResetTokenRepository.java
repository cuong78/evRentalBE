package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.PasswordResetToken;
import com.group4.evRentalBE.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);

    void deleteByUser(User user);
}
