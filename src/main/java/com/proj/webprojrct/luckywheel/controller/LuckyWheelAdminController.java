package com.proj.webprojrct.luckywheel.controller;

import com.proj.webprojrct.luckywheel.entity.LuckyWheelConfig;
import com.proj.webprojrct.luckywheel.entity.LuckyWheelReward;
import com.proj.webprojrct.luckywheel.repository.LuckyWheelConfigRepository;
import com.proj.webprojrct.luckywheel.repository.LuckyWheelRewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin endpoint for Lucky Wheel management
 */
@RestController
@RequestMapping("/api/v1/admin/lucky-wheel")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class LuckyWheelAdminController {

    private final LuckyWheelRewardRepository rewardRepository;
    private final LuckyWheelConfigRepository configRepository;

    /**
     * Setup/Reset lucky wheel rewards theo yêu cầu mới
     * 35%: +100 coin, 25%: +200 coin, 15%: +500 coin, 
     * 10%: +1000 coin, 10%: Chúc bạn may mắn (0 coin), 5%: +2000 coin
     */
    @PostMapping("/setup-rewards")
    public ResponseEntity<Map<String, Object>> setupRewards() {
        try {
            log.info("Setting up lucky wheel rewards with new configuration...");
            
            // Clear existing rewards
            rewardRepository.deleteAll();
            
            // Create rewards theo tỉ lệ yêu cầu (dùng weight để tính xác suất)
            List<LuckyWheelReward> rewards = new ArrayList<>();
            
            // 35%: +100 coin
            rewards.add(createReward(0, "COIN", 100, 35, "ic_coin", "+100 Coin"));
            
            // 25%: +200 coin
            rewards.add(createReward(1, "COIN", 200, 25, "ic_coin_stack", "+200 Coin"));
            
            // 15%: +500 coin
            rewards.add(createReward(2, "COIN", 500, 15, "ic_coin_medium", "+500 Coin"));
            
            // 10%: +1000 coin
            rewards.add(createReward(3, "COIN", 1000, 10, "ic_coin_large", "+1,000 Coin"));
            
            // 10%: Chúc bạn may mắn (0 coin)
            rewards.add(createReward(4, "NOTHING", 0, 10, "ic_sad", "Chúc bạn may mắn lần sau"));
            
            // 5%: +2000 coin
            rewards.add(createReward(5, "COIN", 2000, 5, "ic_jackpot", "+2,000 Coin"));
            
            // Thêm 2 ô nữa để đủ 8 ô (optional)
            rewards.add(createReward(6, "COIN", 100, 0, "ic_coin_tiny", "+100 Coin"));
            rewards.add(createReward(7, "COIN", 200, 0, "ic_coin_small", "+200 Coin"));
            
            // Save all
            List<LuckyWheelReward> saved = rewardRepository.saveAll(rewards);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lucky wheel rewards setup successfully");
            response.put("rewardsCount", saved.size());
            response.put("rewards", saved);
            
            log.info("✅ Successfully setup {} rewards", saved.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to setup rewards", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to setup rewards: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get all rewards
     */
    @GetMapping("/rewards")
    public ResponseEntity<List<LuckyWheelReward>> getAllRewards() {
        List<LuckyWheelReward> rewards = rewardRepository.findAll();
        return ResponseEntity.ok(rewards);
    }
    
    /**
     * Create a new reward
     */
    @PostMapping("/rewards")
    public ResponseEntity<LuckyWheelReward> createReward(@RequestBody LuckyWheelReward reward) {
        try {
            // Tính probability từ weight
            int totalWeight = rewardRepository.findAll().stream()
                    .mapToInt(LuckyWheelReward::getWeight)
                    .sum() + reward.getWeight();
            
            BigDecimal probability = BigDecimal.valueOf(reward.getWeight())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);
            
            reward.setProbability(probability);
            
            LuckyWheelReward saved = rewardRepository.save(reward);
            
            // Cập nhật lại probability cho tất cả rewards
            recalculateProbabilities();
            
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Failed to create reward", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update a reward
     */
    @PutMapping("/rewards/{id}")
    public ResponseEntity<LuckyWheelReward> updateReward(@PathVariable Long id, 
                                                         @RequestBody LuckyWheelReward reward) {
        try {
            LuckyWheelReward existing = rewardRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reward not found"));
            
            existing.setPosition(reward.getPosition());
            existing.setRewardType(reward.getRewardType());
            existing.setCoinAmount(reward.getCoinAmount());
            existing.setWeight(reward.getWeight());
            existing.setIconName(reward.getIconName());
            existing.setLabel(reward.getLabel());
            existing.setIsActive(reward.getIsActive());
            
            LuckyWheelReward updated = rewardRepository.save(existing);
            
            // Cập nhật lại probability cho tất cả rewards
            recalculateProbabilities();
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Failed to update reward", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete a reward
     */
    @DeleteMapping("/rewards/{id}")
    public ResponseEntity<Map<String, Object>> deleteReward(@PathVariable Long id) {
        try {
            rewardRepository.deleteById(id);
            
            // Cập nhật lại probability cho tất cả rewards
            recalculateProbabilities();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reward deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to delete reward", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete reward: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Toggle lucky wheel on/off
     */
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleLuckyWheel(@RequestParam boolean enabled) {
        try {
            LuckyWheelConfig config = configRepository.findByConfigKey("lucky_wheel_enabled")
                    .orElse(LuckyWheelConfig.builder()
                            .configKey("lucky_wheel_enabled")
                            .description("Enable/disable lucky wheel feature")
                            .build());
            
            config.setConfigValue(String.valueOf(enabled));
            configRepository.save(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("enabled", enabled);
            response.put("message", enabled ? "Đã bật vòng quay may mắn" : "Đã tắt vòng quay may mắn");
            
            log.info("Lucky wheel {} by admin", enabled ? "enabled" : "disabled");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to toggle lucky wheel", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to toggle: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get lucky wheel status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean enabled = configRepository.findByConfigKey("lucky_wheel_enabled")
                .map(config -> "true".equalsIgnoreCase(config.getConfigValue()))
                .orElse(true);
        
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", enabled);
        response.put("totalRewards", rewardRepository.count());
        response.put("activeRewards", rewardRepository.findAllByIsActiveTrueOrderByPositionAsc().size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Recalculate probabilities for all rewards based on weights
     */
    private void recalculateProbabilities() {
        List<LuckyWheelReward> rewards = rewardRepository.findAll();
        
        int totalWeight = rewards.stream()
                .mapToInt(LuckyWheelReward::getWeight)
                .sum();
        
        if (totalWeight == 0) {
            return;
        }
        
        for (LuckyWheelReward reward : rewards) {
            BigDecimal probability = BigDecimal.valueOf(reward.getWeight())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);
            
            reward.setProbability(probability);
        }
        
        rewardRepository.saveAll(rewards);
        
        log.info("Recalculated probabilities for {} rewards (total weight: {})", 
                rewards.size(), totalWeight);
    }
    
    private LuckyWheelReward createReward(int position, String type, Integer coinAmount, 
                                         int weight, String iconName, String label) {
        // Probability sẽ được tính sau khi có tổng weight
        BigDecimal probability = weight > 0 ? BigDecimal.valueOf(weight) : BigDecimal.ZERO;
        
        return LuckyWheelReward.builder()
                .position(position)
                .rewardType(type)
                .coinAmount(coinAmount)
                .weight(weight)
                .probability(probability)
                .iconName(iconName)
                .label(label)
                .isActive(true)
                .build();
    }
}
