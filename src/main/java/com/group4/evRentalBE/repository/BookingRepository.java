package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    
    @Query("SELECT b FROM Booking b WHERE b.station.id = :stationId " +
           "AND b.type.id = :typeId " +
           "AND b.status NOT IN ('CANCELLED', 'COMPLETED', 'EXPIRED') " +
           "AND ((b.startDate <= :endDate AND b.endDate >= :startDate))")
    List<Booking> findOverlappingBookings(@Param("stationId") Long stationId,
                                        @Param("typeId") Long typeId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    List<Booking> findByStatusAndPaymentExpiryTimeBefore(Booking.BookingStatus status, LocalDateTime paymentExpiryTime);
    List<Booking> findByCustomerId(Long customerId);
}
