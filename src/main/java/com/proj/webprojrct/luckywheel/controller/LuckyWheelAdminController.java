package com.proj.webprojrct.luckywheel.controller;

import com.proj.webprojrct.luckywheel.entity.LuckyWheelReward;
import com.proj.webprojrct.luckywheel.repository.LuckyWheelRewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin endpoint for Lucky Wheel setup
 */
@RestController
@RequestMapping("/api/v1/admin/lucky-wheel")
@RequiredArgsConstructor
@Slf4j
public class LuckyWheelAdminController {

    private final LuckyWheelRewardRepository rewardRepository;

    /**
     * Setup/Reset lucky wheel rewards
     */
    @PostMapping("/setup-rewards")
    public ResponseEntity<Map<String, Object>> setupRewards() {
        try {
            log.info("Setting up lucky wheel rewards...");
            
            // Clear existing rewards
            rewardRepository.deleteAll();
            
            // Create 8 rewards
            List<LuckyWheelReward> rewards = new ArrayList<>();
            
            rewards.add(createReward(0, "COIN", 1000, 30.00, "ic_coin", "1,000 Coin"));
            rewards.add(createReward(1, "NOTHING", null, 20.00, "ic_sad", "Chúc bạn may mắn lần sau"));
            rewards.add(createReward(2, "COIN", 2000, 15.00, "ic_coin_stack", "2,000 Coin"));
            rewards.add(createReward(3, "COIN", 500, 15.00, "ic_coin_small", "500 Coin"));
            rewards.add(createReward(4, "COIN", 100, 10.00, "ic_coin_tiny", "100 Coin"));
            rewards.add(createReward(5, "COIN", 1500, 7.00, "ic_coin", "1,500 Coin"));
            rewards.add(createReward(6, "COIN", 50, 2.00, "ic_coin_micro", "50 Coin"));
            rewards.add(createReward(7, "COIN", 10000, 1.00, "ic_jackpot", "JACKPOT 10,000 Coin"));
            
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
     * Get all rewards (for testing)
     */
    @GetMapping("/rewards")
    public ResponseEntity<List<LuckyWheelReward>> getAllRewards() {
        List<LuckyWheelReward> rewards = rewardRepository.findAll();
        return ResponseEntity.ok(rewards);
    }
    
    private LuckyWheelReward createReward(int position, String type, Integer coinAmount, 
                                         double probability, String iconName, String label) {
        return LuckyWheelReward.builder()
                .position(position)
                .rewardType(type)
                .coinAmount(coinAmount)
                .probability(BigDecimal.valueOf(probability))
                .iconName(iconName)
                .label(label)
                .isActive(true)
                .build();
    }
}
