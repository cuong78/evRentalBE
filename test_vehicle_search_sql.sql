-- ============================================
-- SQL Query Test Cases
-- Để verify logic Native SQL query
-- ============================================

-- ============================================
-- SETUP TEST DATA
-- ============================================

-- Tạo test station
INSERT INTO rental_station (id, city, address, created_at, updated_at) 
VALUES (999, 'Test City', 'Test Address', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Tạo test vehicle type
INSERT INTO vehicle_type (id, name, deposit_amount, rental_rate, created_at, updated_at)
VALUES (999, 'Test Type', 2000000, 150000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Tạo test vehicles
INSERT INTO vehicle (id, type_id, station_id, status, created_at, updated_at)
VALUES 
    (9991, 999, 999, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9992, 999, 999, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9993, 999, 999, 'RENTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9994, 999, 999, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- ============================================
-- TEST CASE 1: Xe đã trả sớm
-- ============================================

-- Vehicle #9991: Booking 2025-01-15 to 2025-01-25, trả 2025-01-23
-- Search: 2025-01-24 to 2025-01-30
-- Expected: AVAILABLE ✅

-- Create booking
INSERT INTO booking (id, user_id, station_id, type_id, start_date, end_date, payment_method, total_payment, status, created_at, updated_at)
VALUES ('TEST-001', 1, 999, 999, 
        '2025-01-15 10:00:00', '2025-01-25 10:00:00', 
        'VNPAY', 3000000, 'COMPLETED', 
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Create contract
INSERT INTO contract (id, booking_id, vehicle_id, document_id, created_at, updated_at)
VALUES (9991, 'TEST-001', 9991, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Create return transaction (trả sớm ngày 23)
INSERT INTO return_transaction (id, booking_id, return_date, refund_method, refund_amount, created_at, updated_at)
VALUES (9991, 'TEST-001', '2025-01-23 14:00:00', 'TRANSFER', 1000000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Test query
SELECT v.id, v.status
FROM vehicle v
WHERE v.station_id = 999
  AND v.type_id = 999
  AND v.status = 'AVAILABLE'
  AND v.id NOT IN (
      SELECT DISTINCT c.vehicle_id
      FROM contract c
      INNER JOIN booking b ON c.booking_id = b.id
      WHERE b.status IN ('CONFIRMED', 'ACTIVE')
        AND (
            (
                EXISTS (SELECT 1 FROM return_transaction rt WHERE rt.booking_id = b.id)
                AND TIMESTAMP '2025-01-24 10:00:00' < (SELECT rt.return_date FROM return_transaction rt WHERE rt.booking_id = b.id LIMIT 1)
                AND TIMESTAMP '2025-01-30 10:00:00' > b.start_date
            )
            OR (
                b.status = 'ACTIVE'
                AND b.end_date < CURRENT_TIMESTAMP
                AND NOT EXISTS (SELECT 1 FROM return_transaction rt WHERE rt.booking_id = b.id)
            )
            OR (
                NOT EXISTS (SELECT 1 FROM return_transaction rt WHERE rt.booking_id = b.id)
                AND (b.status = 'CONFIRMED' OR (b.status = 'ACTIVE' AND b.end_date >= CURRENT_TIMESTAMP))
                AND TIMESTAMP '2025-01-24 10:00:00' < b.end_date
                AND TIMESTAMP '2025-01-30 10:00:00' > b.start_date
            )
        )
  );
-- Expected: Vehicle 9991 ✅ (vì 24/01 > 23/01 returnDate)

-- ============================================
-- TEST CASE 2: Xe quá hạn chưa trả
-- ============================================

-- Vehicle #9992: Booking 2025-01-10 to 2025-01-15, ACTIVE, no return
-- Today: 2025-01-17
-- Search: 2025-01-17 to 2025-01-20
-- Expected: NOT AVAILABLE ❌

INSERT INTO booking (id, user_id, station_id, type_id, start_date, end_date, payment_method, total_payment, status, created_at, updated_at)
VALUES ('TEST-002', 1, 999, 999,
        '2025-01-10 10:00:00', '2025-01-15 10:00:00',
        'VNPAY', 2000000, 'ACTIVE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO contract (id, booking_id, vehicle_id, document_id, created_at, updated_at)
VALUES (9992, 'TEST-002', 9992, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- No return transaction!

-- Test query (assuming today is after 2025-01-15)
-- Vehicle 9992 should be in conflict list
SELECT DISTINCT c.vehicle_id
FROM contract c
INNER JOIN booking b ON c.booking_id = b.id
WHERE c.vehicle_id = 9992
  AND b.status = 'ACTIVE'
  AND b.end_date < CURRENT_TIMESTAMP
  AND NOT EXISTS (SELECT 1 FROM return_transaction rt WHERE rt.booking_id = b.id);
-- Expected: 9992 ❌ (xe bị block)

-- ============================================
-- TEST CASE 3: Xe đang thuê overlap
-- ============================================

-- Vehicle #9994: Booking 2025-01-15 to 2025-01-25, ACTIVE
-- Search: 2025-01-20 to 2025-01-28
-- Expected: NOT AVAILABLE ❌

INSERT INTO booking (id, user_id, station_id, type_id, start_date, end_date, payment_method, total_payment, status, created_at, updated_at)
VALUES ('TEST-003', 1, 999, 999,
        '2025-01-15 10:00:00', '2025-01-25 10:00:00',
        'VNPAY', 2500000, 'ACTIVE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO contract (id, booking_id, vehicle_id, document_id, created_at, updated_at)
VALUES (9994, 'TEST-003', 9994, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Test overlap detection
SELECT DISTINCT c.vehicle_id
FROM contract c
INNER JOIN booking b ON c.booking_id = b.id
WHERE c.vehicle_id = 9994
  AND NOT EXISTS (SELECT 1 FROM return_transaction rt WHERE rt.booking_id = b.id)
  AND (b.status = 'CONFIRMED' OR (b.status = 'ACTIVE' AND b.end_date >= CURRENT_TIMESTAMP))
  AND TIMESTAMP '2025-01-20 10:00:00' < b.end_date
  AND TIMESTAMP '2025-01-28 10:00:00' > b.start_date;
-- Expected: 9994 ❌ (có overlap)

-- ============================================
-- FULL QUERY TEST
-- Search: 2025-01-24 to 2025-01-30
-- ============================================

SELECT DISTINCT v.id, v.status
FROM vehicle v
WHERE v.station_id = 999
  AND v.type_id = 999
  AND v.status = 'AVAILABLE'
  AND v.id NOT IN (
      SELECT DISTINCT c.vehicle_id
      FROM contract c
      INNER JOIN booking b ON c.booking_id = b.id
      WHERE b.status IN ('CONFIRMED', 'ACTIVE')
        AND (
            (
                EXISTS (SELECT 1 FROM return_transaction rt WHERE rt.booking_id = b.id)
                AND TIMESTAMP '2025-01-24 10:00:00' < (SELECT rt.return_date FROM return_transaction rt WHERE rt.booking_id = b.id LIMIT 1)
                AND TIMESTAMP '2025-01-30 10:00:00' > b.start_date
            )
            OR (
                b.status = 'ACTIVE'
                AND b.end_date < CURRENT_TIMESTAMP
                AND NOT EXISTS (SELECT 1 FROM return_transaction rt WHERE rt.booking_id = b.id)
            )
            OR (
                NOT EXISTS (SELECT 1 FROM return_transaction rt WHERE rt.booking_id = b.id)
                AND (b.status = 'CONFIRMED' OR (b.status = 'ACTIVE' AND b.end_date >= CURRENT_TIMESTAMP))
                AND TIMESTAMP '2025-01-24 10:00:00' < b.end_date
                AND TIMESTAMP '2025-01-30 10:00:00' > b.start_date
            )
        )
  )
ORDER BY v.id;

-- Expected results:
-- 9991 ✅ (trả rồi từ 23/01, search từ 24/01)
-- 9992 ❌ (quá hạn chưa trả - blocked)
-- 9993 ❌ (status = RENTED)
-- 9994 ❌ (đang thuê 15-25, overlap với 24-30? NO! 24 > 25 = FALSE)
-- Actually 9994 ✅ nếu search 24-30 và booking end 25/01

-- ============================================
-- CLEANUP
-- ============================================

-- DELETE FROM return_transaction WHERE id >= 9991;
-- DELETE FROM contract WHERE id >= 9991;
-- DELETE FROM booking WHERE id LIKE 'TEST-%';
-- DELETE FROM vehicle WHERE id >= 9991;
-- DELETE FROM vehicle_type WHERE id = 999;
-- DELETE FROM rental_station WHERE id = 999;
