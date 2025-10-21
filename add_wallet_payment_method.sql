-- Migration script to add WALLET payment method support
-- Date: 2025-10-21
-- Description: Updates payment_method_check constraint to allow WALLET payment method

-- Step 1: Drop existing constraint
ALTER TABLE payment DROP CONSTRAINT IF EXISTS payment_method_check;

-- Step 2: Add new constraint with WALLET included
ALTER TABLE payment ADD CONSTRAINT payment_method_check 
    CHECK (method IN ('CASH', 'VNPAY', 'WALLET'));

-- Verify the constraint
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conname = 'payment_method_check';

-- Optional: Check existing payment methods in database
SELECT DISTINCT method FROM payment;
