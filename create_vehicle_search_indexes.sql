-- ============================================
-- Tạo Indexes để tối ưu Vehicle Search Query
-- ============================================

-- Index cho Vehicle table
CREATE INDEX IF NOT EXISTS idx_vehicle_station_type_status 
ON vehicle(station_id, type_id, status);

-- Index cho Contract table
CREATE INDEX IF NOT EXISTS idx_contract_vehicle_booking 
ON contract(vehicle_id, booking_id);

-- Index cho Booking table
CREATE INDEX IF NOT EXISTS idx_booking_status_dates 
ON booking(status, start_date, end_date);

-- Composite index cho booking
CREATE INDEX IF NOT EXISTS idx_booking_id_status 
ON booking(id, status);

-- Index cho ReturnTransaction table
CREATE INDEX IF NOT EXISTS idx_return_transaction_booking_date 
ON return_transaction(booking_id, return_date);

-- ============================================
-- Verify Indexes
-- ============================================
-- Run this to check if indexes were created:
-- SELECT indexname, tablename FROM pg_indexes WHERE tablename IN ('vehicle', 'contract', 'booking', 'return_transaction');
