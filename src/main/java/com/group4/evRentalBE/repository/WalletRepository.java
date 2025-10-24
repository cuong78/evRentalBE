package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.User;
import com.group4.evRentalBE.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserUserId(Long userId);

    boolean existsByUser(User customer);
}
