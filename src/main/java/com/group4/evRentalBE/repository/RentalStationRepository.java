package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.RentalStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalStationRepository extends JpaRepository<RentalStation, Long> {
}
