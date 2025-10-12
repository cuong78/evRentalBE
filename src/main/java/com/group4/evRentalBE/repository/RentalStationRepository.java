package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.RentalStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalStationRepository extends JpaRepository<RentalStation, Long> {
    List<RentalStation> findByCity(String city);
    boolean existsByCityAndAddress(String city, String address);
    Optional<RentalStation> findByAdminId(Long adminId);
}
