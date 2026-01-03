-- Migration script to add event management fields to wheel_config
-- Thêm các trường quản lý sự kiện vòng quay

ALTER TABLE wheel_config ADD COLUMN IF NOT EXISTS event_name VARCHAR(255);
ALTER TABLE wheel_config ADD COLUMN IF NOT EXISTS start_date TIMESTAMP;
ALTER TABLE wheel_config ADD COLUMN IF NOT EXISTS end_date TIMESTAMP;
ALTER TABLE wheel_config ADD COLUMN IF NOT EXISTS is_time_restricted BOOLEAN NOT NULL DEFAULT false;

-- Comments
COMMENT ON COLUMN wheel_config.event_name IS 'Tên sự kiện (vd: Tết Nguyên Đán 2026, Black Friday)';
COMMENT ON COLUMN wheel_config.start_date IS 'Ngày bắt đầu sự kiện';
COMMENT ON COLUMN wheel_config.end_date IS 'Ngày kết thúc sự kiện';
COMMENT ON COLUMN wheel_config.is_time_restricted IS 'Có giới hạn theo thời gian không';

-- Update existing config with default values
UPDATE wheel_config 
SET is_time_restricted = false 
WHERE is_time_restricted IS NULL;
