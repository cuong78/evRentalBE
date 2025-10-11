package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.StaffReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffReturnRepository extends JpaRepository<StaffReturn, Long> {
}
