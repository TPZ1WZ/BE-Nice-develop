package com.proj.webprojrct.luckywheel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/lucky-wheel/migration")
@PreAuthorize("hasRole('ADMIN')")
public class LuckyWheelMigrationController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/reset-rewards")
    public ResponseEntity<Map<String, Object>> resetRewards() {
        try {
            // Delete old rewards
            jdbcTemplate.update("DELETE FROM lucky_wheel_rewards");

            // Insert new rewards with updated probabilities
            String sql = """
                INSERT INTO lucky_wheel_rewards (position, reward_type, coin_amount, weight, probability, icon_name, label, is_active, created_at)
                VALUES 
                    (0, 'COIN', 100, 80, 40.00, 'ic_coin', '+100 Coin', true, CURRENT_TIMESTAMP),
                    (1, 'COIN', 200, 50, 25.00, 'ic_coin_stack', '+200 Coin', true, CURRENT_TIMESTAMP),
                    (2, 'COIN', 300, 30, 15.00, 'ic_coin_medium', '+300 Coin', true, CURRENT_TIMESTAMP),
                    (3, 'COIN', 400, 16, 8.00, 'ic_coin_large', '+400 Coin', true, CURRENT_TIMESTAMP),
                    (4, 'COIN', 500, 10, 5.00, 'ic_coin_xlarge', '+500 Coin', true, CURRENT_TIMESTAMP),
                    (5, 'COIN', 1000, 3, 1.50, 'ic_coin_xxlarge', '+1,000 Coin', true, CURRENT_TIMESTAMP),
                    (6, 'COIN', 2000, 1, 0.50, 'ic_jackpot', '+2,000 Coin', true, CURRENT_TIMESTAMP),
                    (7, 'NOTHING', 0, 10, 5.00, 'ic_sad', 'Chúc bạn may mắn lần sau', true, CURRENT_TIMESTAMP)
                """;
            
            jdbcTemplate.update(sql);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã reset phần thưởng thành công");
            response.put("rewards", Map.of(
                "+100 coin", "40%",
                "+200 coin", "25%",
                "+300 coin", "15%",
                "+400 coin", "8%",
                "+500 coin", "5%",
                "+1000 coin", "1.5%",
                "+2000 coin", "0.5%",
                "Chúc may mắn", "5%"
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/fix-spin-history-schema")
    public ResponseEntity<Map<String, Object>> fixSpinHistorySchema() {
        try {
            // Cho phép prize_id NULL vì không còn được sử dụng
            jdbcTemplate.execute("ALTER TABLE spin_history ALTER COLUMN prize_id DROP NOT NULL");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã fix schema spin_history (prize_id cho phép NULL)");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
