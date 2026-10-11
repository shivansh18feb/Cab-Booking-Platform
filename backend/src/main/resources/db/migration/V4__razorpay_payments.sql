-- ==============================================================================
-- Migration V4: Transition payments schema to Razorpay
-- ==============================================================================

-- Add provider payment id (e.g. pay_...) and signature columns
ALTER TABLE payments ADD COLUMN IF NOT EXISTS provider_payment_id VARCHAR(100);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS provider_signature VARCHAR(255);

-- Set default payment provider and currency to Razorpay and INR
ALTER TABLE payments ALTER COLUMN payment_provider SET DEFAULT 'RAZORPAY';
ALTER TABLE payments ALTER COLUMN currency SET DEFAULT 'INR';

-- Update existing records if any
UPDATE payments SET payment_provider = 'RAZORPAY' WHERE payment_provider = 'STRIPE';

-- Add index on provider_payment_id
CREATE INDEX IF NOT EXISTS idx_payments_provider_payment_id ON payments(provider_payment_id);
