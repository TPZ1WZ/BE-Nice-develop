-- Migration: Change order_item.product_image column from VARCHAR to TEXT
-- Purpose: Support base64 encoded images which can be very long

-- Change column type to TEXT to support long base64 strings
ALTER TABLE order_item ALTER COLUMN product_image TYPE TEXT;

-- Add comment
COMMENT ON COLUMN order_item.product_image IS 'Product image URL or base64 encoded image snapshot at time of purchase';
