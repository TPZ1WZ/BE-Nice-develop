-- Insert dữ liệu rewards cho 7 ngày checkin
INSERT INTO daily_checkin_rewards (day_number, reward_amount, is_bonus, description)
VALUES 
    (1, 1000, FALSE, 'Day 1 reward'),
    (2, 1000, FALSE, 'Day 2 reward'),
    (3, 1000, FALSE, 'Day 3 reward'),
    (4, 1000, FALSE, 'Day 4 reward'),
    (5, 1000, FALSE, 'Day 5 reward'),
    (6, 1000, FALSE, 'Day 6 reward'),
    (7, 4000, TRUE, 'Day 7 bonus reward')
ON CONFLICT (day_number) DO NOTHING;

-- Kiểm tra lại
SELECT * FROM daily_checkin_rewards ORDER BY day_number;
