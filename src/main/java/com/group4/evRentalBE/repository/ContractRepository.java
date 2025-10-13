package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Optional<Contract> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
}
