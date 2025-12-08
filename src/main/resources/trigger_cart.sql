-- ==============================
-- Function cập nhật tổng giỏ hàng
-- ==============================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_proc WHERE proname = 'update_cart_totals'
    ) THEN
        CREATE OR REPLACE FUNCTION update_cart_totals()
        RETURNS TRIGGER AS $body$
BEGIN
UPDATE carts
SET total_price = (
    SELECT COALESCE(SUM(quantity * product_price), 0)
    FROM cart_items
    WHERE cart_id = NEW.cart_id
),
    total_quantity = (
        SELECT COALESCE(SUM(quantity), 0)
        FROM cart_items
        WHERE cart_id = NEW.cart_id
    ),
    updated_at = CURRENT_TIMESTAMP
WHERE id = NEW.cart_id;

RETURN NEW;
END;
        $body$ LANGUAGE plpgsql;
END IF;
END $$;

-- ==============================
-- Trigger tự động cập nhật
-- ==============================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_update_cart_totals'
    ) THEN
CREATE TRIGGER trg_update_cart_totals
    AFTER INSERT OR UPDATE OR DELETE
                    ON cart_items
                        FOR EACH ROW
                        EXECUTE FUNCTION update_cart_totals();
END IF;
END $$;
