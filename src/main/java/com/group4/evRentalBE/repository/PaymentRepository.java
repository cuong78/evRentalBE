package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.Booking;
import com.group4.evRentalBE.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find the most recent payment for a booking with specific type and status
     * @param booking The booking
     * @param type The payment type
     * @param status The payment status
     * @return Optional containing the payment if found
     */
    Optional<Payment> findFirstByBookingAndTypeAndStatusOrderByCreatedAtDesc(
            Booking booking, 
            Payment.PaymentType type, 
            Payment.PaymentStatus status
    );
}
