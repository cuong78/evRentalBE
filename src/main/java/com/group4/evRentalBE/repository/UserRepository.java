package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
}
