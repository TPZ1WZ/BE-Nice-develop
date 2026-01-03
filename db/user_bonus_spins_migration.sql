-- Create user_bonus_spins table
CREATE TABLE IF NOT EXISTS user_bonus_spins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bonus_spins INTEGER NOT NULL DEFAULT 0,
    granted_at TIMESTAMP,
    granted_by VARCHAR(255),
    reason TEXT,
    CONSTRAINT fk_user_bonus_spins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_bonus UNIQUE(user_id)
);

CREATE INDEX idx_user_bonus_spins_user_id ON user_bonus_spins(user_id);

COMMENT ON TABLE user_bonus_spins IS 'Lưu trữ lượt quay bonus được tặng cho user';
COMMENT ON COLUMN user_bonus_spins.bonus_spins IS 'Số lượt quay bonus còn lại';
COMMENT ON COLUMN user_bonus_spins.granted_at IS 'Thời điểm tặng lượt quay gần nhất';
COMMENT ON COLUMN user_bonus_spins.granted_by IS 'Admin đã tặng lượt quay';
