-- Rollback script for WALLET payment method
-- Date: 2025-10-21
-- Description: Reverts payment_method_check constraint to original state (only CASH and VNPAY)

-- CAUTION: This will fail if there are existing WALLET payment records in the database
-- Make sure to delete or update those records first

-- Step 1: Drop current constraint
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_method_check;

-- Step 2: Restore original constraint (without WALLET)
ALTER TABLE payment ADD CONSTRAINT payment_method_check 
    CHECK (method IN ('CASH', 'VNPAY'));

-- Verify the constraint
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conname = 'payment_method_check';
