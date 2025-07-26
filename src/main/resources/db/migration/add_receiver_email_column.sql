-- Add receiver_email column to orders table
ALTER TABLE orders 
ADD COLUMN receiver_email VARCHAR(100) NOT NULL DEFAULT '';

-- Update existing orders with placeholder email if needed
-- You may want to update this with real emails if you have them
UPDATE orders 
SET receiver_email = CONCAT('customer', id, '@placeholder.com') 
WHERE receiver_email = '';

-- Remove the default constraint after populating existing records
ALTER TABLE orders 
ALTER COLUMN receiver_email DROP DEFAULT;
