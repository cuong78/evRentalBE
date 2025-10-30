package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.Booking;
import com.group4.evRentalBE.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {


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
    @Query("""
        SELECT p FROM Payment p
        JOIN p.booking b
        WHERE (:stationId IS NULL OR b.station.id = :stationId)
          AND (:typeId IS NULL OR b.type.id = :typeId)
        ORDER BY p.paymentDate DESC
    """)
    List<Payment> findByStationAndType(
            @Param("stationId") Long stationId,
            @Param("typeId") Long typeId
    );


    @Query("""
        SELECT p FROM Payment p
        JOIN p.booking b
        WHERE (:stationId IS NULL OR b.station.id = :stationId)
          AND (:typeId IS NULL OR b.type.id = :typeId)
          AND (:startDate IS NULL OR p.paymentDate >= :startDate)
          AND (:endDate IS NULL OR p.paymentDate <= :endDate)
        ORDER BY p.paymentDate DESC
    """)
    List<Payment> findFilteredPayments(
            @Param("stationId") Long stationId,
            @Param("typeId") Long typeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT p FROM Payment p " +
            "JOIN p.booking b " +
            "WHERE (:stationId IS NULL OR b.station.id = :stationId) " +
            "AND (:typeId IS NULL OR b.type.id = :typeId) " +
            "AND (:startDate IS NULL OR p.paymentDate >= :startDate) " +
            "AND (:endDate IS NULL OR p.paymentDate <= :endDate) " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> filterPayments(@Param("stationId") Long stationId,
                                 @Param("typeId") Long typeId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

}
