package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);
    Optional<Permission> findByName(String name);
    boolean existsByCode(String code);
}
