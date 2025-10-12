package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByStationId(Long stationId);
    List<Vehicle> findByTypeId(Long typeId);
    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);
    List<Vehicle> findByStationIdAndStatus(Long stationId, Vehicle.VehicleStatus status);
    boolean existsByTypeId(Long typeId);
    boolean existsByStationId(Long stationId);
}
