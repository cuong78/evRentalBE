package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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
    List<Booking> findByUserUserId(Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.userId = :userId AND b.status = 'PENDING'")
    long countPendingBookingsByUserId(@Param("userId") Long userId);
    
    List<Booking> findByUserUserIdAndStatusOrderByCreatedAtDesc(Long userId, Booking.BookingStatus status);
    
    // Find bookings by status
    List<Booking> findByStatusOrderByCreatedAtDesc(Booking.BookingStatus status);
    
    // Count reserved vehicles (CONFIRMED or ACTIVE) for a specific station, type, and date range
    @Query("SELECT COUNT(b) FROM Booking b WHERE " +
           "b.station.id = :stationId AND " +
           "b.type.id = :typeId AND " +
           "(b.status = 'CONFIRMED' OR b.status = 'ACTIVE') AND " +
           "b.startDate < :endDate AND b.endDate > :startDate")
    long countReservedVehicles(@Param("stationId") Long stationId,
                              @Param("typeId") Long typeId,
                              @Param("startDate") java.time.LocalDate startDate,
                              @Param("endDate") java.time.LocalDate endDate);
    
    // Count methods for user profile
    long countByUserUserId(Long userId);
    long countByUserUserIdAndStatus(Long userId, Booking.BookingStatus status);


}
