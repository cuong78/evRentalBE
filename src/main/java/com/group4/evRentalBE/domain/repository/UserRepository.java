package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
    // Lấy theo role name
    @Query("select u from User u join u.roles r where r.name = :roleName")
    List<User> findAllByRole(@Param("roleName") String roleName);
}
