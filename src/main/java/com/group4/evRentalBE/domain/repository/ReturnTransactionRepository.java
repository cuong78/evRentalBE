package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.ReturnTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReturnTransactionRepository extends JpaRepository<ReturnTransaction, Long>, JpaSpecificationExecutor<ReturnTransaction> {
    Optional<ReturnTransaction> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
}
