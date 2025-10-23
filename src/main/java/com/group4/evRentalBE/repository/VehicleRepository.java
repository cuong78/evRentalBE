package com.group4.evRentalBE.repository;

import com.group4.evRentalBE.model.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByStationId(Long stationId);
    List<Vehicle> findByTypeId(Long typeId);

    List<Vehicle> findByStationIdAndTypeId(Long stationId, Long typeId);


    @Query(value = """
        SELECT DISTINCT v.* 
        FROM vehicle v
        WHERE v.station_id = :stationId
          AND v.type_id = :typeId
          AND v.status = 'AVAILABLE'
          AND v.id NOT IN (
              -- Subquery: Tìm các xe BỊ CONFLICT
              SELECT DISTINCT c.vehicle_id
              FROM contract c
              INNER JOIN booking b ON c.booking_id = b.id
              WHERE b.status IN ('CONFIRMED', 'ACTIVE')
                AND (
                    -- Case 1: Có ReturnTransaction - dùng returnDate
                    (
                        EXISTS (
                            SELECT 1 
                            FROM return_transaction rt 
                            WHERE rt.booking_id = b.id
                        )
                        AND :startDate < (
                            SELECT rt.return_date 
                            FROM return_transaction rt 
                            WHERE rt.booking_id = b.id 
                            LIMIT 1
                        )
                        AND :endDate > b.start_date
                    )
                    -- Case 2: ACTIVE quá hạn chưa trả - block hoàn toàn
                    OR (
                        b.status = 'ACTIVE'
                        AND b.end_date < CURRENT_TIMESTAMP
                        AND NOT EXISTS (
                            SELECT 1 
                            FROM return_transaction rt 
                            WHERE rt.booking_id = b.id
                        )
                    )
                    -- Case 3: CONFIRMED hoặc ACTIVE chưa quá hạn - check overlap
                    OR (
                        NOT EXISTS (
                            SELECT 1 
                            FROM return_transaction rt 
                            WHERE rt.booking_id = b.id
                        )
                        AND (b.status = 'CONFIRMED' OR (b.status = 'ACTIVE' AND b.end_date >= CURRENT_TIMESTAMP))
                        AND :startDate < b.end_date
                        AND :endDate > b.start_date
                    )
                )
          )
        ORDER BY v.id
        """, nativeQuery = true)
    List<Vehicle> findAvailableVehiclesForPeriod(
            @Param("stationId") Long stationId,
            @Param("typeId") Long typeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Tìm TẤT CẢ xe available tại station trong khoảng thời gian (không filter theo type)
     * Dùng để lấy tất cả loại xe available
     */
    @Query(value = """
        SELECT DISTINCT v.* 
        FROM vehicle v
        WHERE v.station_id = :stationId
          AND v.status = 'AVAILABLE'
          AND v.id NOT IN (
              SELECT DISTINCT c.vehicle_id
              FROM contract c
              INNER JOIN booking b ON c.booking_id = b.id
              WHERE b.status IN ('CONFIRMED', 'ACTIVE')
                AND (
                    (
                        EXISTS (
                            SELECT 1 
                            FROM return_transaction rt 
                            WHERE rt.booking_id = b.id
                        )
                        AND :startDate < (
                            SELECT rt.return_date 
                            FROM return_transaction rt 
                            WHERE rt.booking_id = b.id 
                            LIMIT 1
                        )
                        AND :endDate > b.start_date
                    )
                    OR (
                        b.status = 'ACTIVE'
                        AND b.end_date < CURRENT_TIMESTAMP
                        AND NOT EXISTS (
                            SELECT 1 
                            FROM return_transaction rt 
                            WHERE rt.booking_id = b.id
                        )
                    )
                    OR (
                        NOT EXISTS (
                            SELECT 1 
                            FROM return_transaction rt 
                            WHERE rt.booking_id = b.id
                        )
                        AND (b.status = 'CONFIRMED' OR (b.status = 'ACTIVE' AND b.end_date >= CURRENT_TIMESTAMP))
                        AND :startDate < b.end_date
                        AND :endDate > b.start_date
                    )
                )
          )
        ORDER BY v.type_id, v.id
        """, nativeQuery = true)
    List<Vehicle> findAvailableVehiclesByStation(
            @Param("stationId") Long stationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.station.id = :stationId AND v.type.id = :typeId")
    long countByStationAndType(@Param("stationId") Long stationId, @Param("typeId") Long typeId);
}
