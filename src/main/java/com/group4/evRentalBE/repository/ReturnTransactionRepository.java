package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.ReturnTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReturnTransactionRepository extends JpaRepository<ReturnTransaction, Long> {
    Optional<ReturnTransaction> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
}
