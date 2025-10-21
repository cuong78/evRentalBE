-- Migration script to update payment methods: only VNPAY and WALLET (remove CASH)
-- Date: 2025-10-21
-- Description: 
--   1. Updates payment_method_check constraint to allow only VNPAY and WALLET
--   2. Removes refund_method column from return_transaction table

-- IMPORTANT: Run this migration AFTER dropping and recreating the database
-- Or manually update existing CASH records before running this script

-- Step 1: Drop existing constraint
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_method_check;

-- Step 2: Add new constraint with only VNPAY and WALLET (CASH removed)
ALTER TABLE payment ADD CONSTRAINT payment_method_check 
    CHECK (method IN ('VNPAY', 'WALLET'));

-- Step 3: Drop refund_method column from return_transaction (refunds always go to wallet now)
ALTER TABLE return_transaction DROP COLUMN IF EXISTS refund_method;

-- Verify the constraint
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conname = 'payment_method_check';

-- Optional: Check existing payment methods in database
SELECT DISTINCT method FROM payment;

-- Optional: Check return_transaction columns
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'return_transaction';
