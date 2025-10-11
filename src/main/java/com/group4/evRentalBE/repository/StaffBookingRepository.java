package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.StaffBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffBookingRepository extends JpaRepository<StaffBooking, Long> {
}
