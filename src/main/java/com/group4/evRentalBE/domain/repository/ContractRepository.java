package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Optional<Contract> findByBookingId(String bookingId);
    boolean existsByBookingId(String bookingId);
    
    /**
     * Check if vehicle has an active contract (booking status is ACTIVE)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Contract c " +
           "WHERE c.vehicle.id = :vehicleId " +
           "AND c.booking.status = 'ACTIVE'")
    boolean existsActiveContractByVehicleId(@Param("vehicleId") Long vehicleId);
}
