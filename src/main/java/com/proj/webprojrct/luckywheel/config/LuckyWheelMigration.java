package com.proj.webprojrct.luckywheel.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Auto-run Lucky Wheel migration on startup
 * Disable this component after first successful run by setting enabled=false
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LuckyWheelMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    
    // Set to false after first successful run
    private static final boolean ENABLED = true;

    @Override
    public void run(String... args) {
        if (!ENABLED) {
            log.info("Lucky Wheel migration is disabled");
            return;
        }

        try {
            log.info("🎰 Running Lucky Wheel migration...");
            
            // Check if migration already ran
            try {
                Long count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM lucky_wheel_config WHERE config_key = 'migration_v2_completed'", 
                    Long.class);
                
                if (count != null && count > 0) {
                    log.info("✅ Migration already completed. Skipping...");
                    return;
                }
            } catch (Exception e) {
                // Table might not exist yet, continue with migration
                log.info("Migration not run yet, proceeding...");
            }
            
            // Run migration steps
            runMigration();
            
            log.info("✅ Lucky Wheel migration completed successfully!");
            
        } catch (Exception e) {
            log.error("❌ Failed to run Lucky Wheel migration: {}", e.getMessage(), e);
            // Don't throw exception to allow app to start
        }
    }

    private void runMigration() {
        // 0. Fix existing spin_history data
        try {
            jdbcTemplate.execute(
                "UPDATE spin_history SET reward_type = 'COIN' WHERE reward_type IS NULL"
            );
            log.info("✅ Fixed null reward_type in spin_history");
            
            // Now set NOT NULL constraint
            jdbcTemplate.execute(
                "ALTER TABLE spin_history ALTER COLUMN reward_type SET NOT NULL"
            );
            log.info("✅ Set NOT NULL constraint on reward_type");
        } catch (Exception e) {
            log.warn("Reward_type already fixed: {}", e.getMessage());
        }
        
        // 1. Add weight column if not exists
        try {
            jdbcTemplate.execute(
                "ALTER TABLE lucky_wheel_rewards " +
                "ADD COLUMN IF NOT EXISTS weight INTEGER DEFAULT 10 NOT NULL"
            );
            log.info("✅ Added weight column to lucky_wheel_rewards");
        } catch (Exception e) {
            log.warn("Weight column might already exist: {}", e.getMessage());
        }

        // 2. Create product_views table
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS product_views (" +
            "    id BIGSERIAL PRIMARY KEY," +
            "    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
            "    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE," +
            "    view_date DATE NOT NULL," +
            "    view_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "    CONSTRAINT unique_user_product_date UNIQUE (user_id, product_id, view_date)" +
            ")"
        );
        log.info("✅ Created product_views table");

        // Create indexes
        jdbcTemplate.execute(
            "CREATE INDEX IF NOT EXISTS idx_product_views_user_date " +
            "ON product_views(user_id, view_date)"
        );
        jdbcTemplate.execute(
            "CREATE INDEX IF NOT EXISTS idx_product_views_product " +
            "ON product_views(product_id)"
        );
        log.info("✅ Created indexes on product_views");

        // 3. Create lucky_wheel_config table
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS lucky_wheel_config (" +
            "    id BIGSERIAL PRIMARY KEY," +
            "    config_key VARCHAR(255) NOT NULL UNIQUE," +
            "    config_value VARCHAR(255) NOT NULL," +
            "    description TEXT," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        log.info("✅ Created lucky_wheel_config table");

        // 4. Insert default config
        jdbcTemplate.execute(
            "INSERT INTO lucky_wheel_config (config_key, config_value, description) " +
            "VALUES ('lucky_wheel_enabled', 'true', 'Enable/disable lucky wheel feature') " +
            "ON CONFLICT (config_key) DO NOTHING"
        );
        log.info("✅ Inserted default config");

        // 5. Update rewards with new configuration
        jdbcTemplate.execute("DELETE FROM lucky_wheel_rewards");
        log.info("✅ Cleared old rewards");

        // 6. Insert new rewards
        jdbcTemplate.execute(
            "INSERT INTO lucky_wheel_rewards (position, reward_type, coin_amount, weight, probability, icon_name, label, is_active, created_at) VALUES " +
            "(0, 'COIN', 100, 35, 35.00, 'ic_coin', '+100 Coin', true, CURRENT_TIMESTAMP)," +
            "(1, 'COIN', 200, 25, 25.00, 'ic_coin_stack', '+200 Coin', true, CURRENT_TIMESTAMP)," +
            "(2, 'COIN', 500, 15, 15.00, 'ic_coin_medium', '+500 Coin', true, CURRENT_TIMESTAMP)," +
            "(3, 'COIN', 1000, 10, 10.00, 'ic_coin_large', '+1,000 Coin', true, CURRENT_TIMESTAMP)," +
            "(4, 'NOTHING', 0, 10, 10.00, 'ic_sad', 'Chúc bạn may mắn lần sau', true, CURRENT_TIMESTAMP)," +
            "(5, 'COIN', 2000, 5, 5.00, 'ic_jackpot', '+2,000 Coin', true, CURRENT_TIMESTAMP)," +
            "(6, 'COIN', 100, 0, 0.00, 'ic_coin_tiny', '+100 Coin', false, CURRENT_TIMESTAMP)," +
            "(7, 'COIN', 200, 0, 0.00, 'ic_coin_small', '+200 Coin', false, CURRENT_TIMESTAMP)"
        );
        log.info("✅ Inserted new rewards with updated configuration");

        // 7. Mark migration as completed
        jdbcTemplate.execute(
            "INSERT INTO lucky_wheel_config (config_key, config_value, description) " +
            "VALUES ('migration_v2_completed', 'true', 'Migration v2.0 completed on " + java.time.LocalDateTime.now() + "') " +
            "ON CONFLICT (config_key) DO NOTHING"
        );
        log.info("✅ Marked migration as completed");
        
        log.info("📊 Phần thưởng:");
        log.info("   - 35%: +100 coin");
        log.info("   - 25%: +200 coin");
        log.info("   - 15%: +500 coin");
        log.info("   - 10%: +1000 coin");
        log.info("   - 10%: Chúc bạn may mắn (0 coin)");
        log.info("   - 5%: +2000 coin");
        log.info("🎯 Giới hạn: 1 lượt/ngày, cần xem 3 sản phẩm");
    }
}
