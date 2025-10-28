package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}

