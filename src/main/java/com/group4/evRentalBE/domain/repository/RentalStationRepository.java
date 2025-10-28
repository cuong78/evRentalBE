package com.group4.evRentalBE.domain.repository;

import com.group4.evRentalBE.domain.entity.RentalStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalStationRepository extends JpaRepository<RentalStation, Long> {
    List<RentalStation> findByCity(String city);
    boolean existsByCityAndAddress(String city, String address);
}
