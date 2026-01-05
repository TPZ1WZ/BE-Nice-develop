package com.proj.webprojrct.luckywheel.service;

import com.proj.webprojrct.luckywheel.dto.LuckyWheelInfoResponse;
import com.proj.webprojrct.luckywheel.dto.SpinResponse;
import com.proj.webprojrct.luckywheel.entity.LuckyWheelReward;
import com.proj.webprojrct.luckywheel.entity.SpinHistory;
import com.proj.webprojrct.luckywheel.repository.LuckyWheelRewardRepository;
import com.proj.webprojrct.luckywheel.repository.SpinHistoryRepository;
import com.proj.webprojrct.loyalty.entity.LoyaltyTransaction;
import com.proj.webprojrct.loyalty.repository.LoyaltyTransactionRepository;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LuckyWheelService {

    private final LuckyWheelRewardRepository rewardRepository;
    private final SpinHistoryRepository spinHistoryRepository;
    private final UserRepository userRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    
    private final Random random = new Random();
    private static final int SPIN_COST = 500; // Chi phí quay = 500 coin
    private static final int MAX_DAILY_SPINS = 999; // Giới hạn số lượt quay mỗi ngày (999 = không giới hạn)

    /**
     * Lấy thông tin vòng quay
     */
    public LuckyWheelInfoResponse getWheelInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean hasFreeSpinToday = spinHistoryRepository.hasFreeSpinToday(userId);
        long todaySpins = spinHistoryRepository.countTodaySpins(userId);
        int totalCoinsWon = spinHistoryRepository.getTotalCoinsWon(userId);
        
        List<LuckyWheelReward> rewards = rewardRepository.findAllByIsActiveTrueOrderByPositionAsc();
        
        List<LuckyWheelInfoResponse.RewardItem> rewardItems = rewards.stream()
                .map(r -> LuckyWheelInfoResponse.RewardItem.builder()
                        .id(r.getId())
                        .position(r.getPosition())
                        .rewardType(r.getRewardType())
                        .coinAmount(r.getCoinAmount())
                        .label(r.getLabel())
                        .iconName(r.getIconName())
                        .build())
                .collect(Collectors.toList());
        
        return LuckyWheelInfoResponse.builder()
                .hasFreeSpinToday(!hasFreeSpinToday) // true nếu chưa quay free
                .spinCost(SPIN_COST)
                .currentPoints(user.getLoyaltyPoints())
                .todaySpins(todaySpins)
                .totalCoinsWon(totalCoinsWon)
                .rewards(rewardItems)
                .build();
    }

    /**
     * Thực hiện quay thưởng
     */
    @Transactional
    public SpinResponse performSpin(Long userId, Boolean useFreeSpin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        boolean hasFreeSpinToday = spinHistoryRepository.hasFreeSpinToday(userId);
        long todaySpins = spinHistoryRepository.countTodaySpins(userId);
        
        // Kiểm tra giới hạn số lượt quay mỗi ngày
        if (todaySpins >= MAX_DAILY_SPINS) {
            return SpinResponse.builder()
                    .success(false)
                    .message("Bạn đã hết lượt quay hôm nay (tối đa " + MAX_DAILY_SPINS + " lượt)")
                    .totalPoints(user.getLoyaltyPoints())
                    .build();
        }
        
        int cost = 0;
        
        // Xác định loại quay
        if (useFreeSpin != null && useFreeSpin) {
            // Dùng lượt free
            if (hasFreeSpinToday) {
                return SpinResponse.builder()
                        .success(false)
                        .message("Bạn đã hết lượt quay miễn phí hôm nay")
                        .build();
            }
            cost = 0;
        } else {
            // Trả coin để quay
            if (user.getLoyaltyPoints() < SPIN_COST) {
                return SpinResponse.builder()
                        .success(false)
                        .message("Không đủ coin để quay (cần " + SPIN_COST + " coin)")
                        .totalPoints(user.getLoyaltyPoints())
                        .build();
            }
            cost = SPIN_COST;
            
            // Trừ coin
            user.setLoyaltyPoints(user.getLoyaltyPoints() - SPIN_COST);
            userRepository.save(user);
            
            // Ghi transaction trừ coin
            LoyaltyTransaction deductTransaction = LoyaltyTransaction.builder()
                    .user(user)
                    .transactionType(LoyaltyTransaction.TransactionType.SPEND)
                    .amount(-SPIN_COST)
                    .source("LUCKY_WHEEL")
                    .description("Chi phí quay vòng may mắn")
                    .balanceAfter(user.getLoyaltyPoints())
                    .build();
            loyaltyTransactionRepository.save(deductTransaction);
        }
        
        // Chọn phần thưởng dựa trên xác suất
        LuckyWheelReward wonReward = selectRewardByProbability();
        
        if (wonReward == null) {
            return SpinResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống vòng quay")
                    .build();
        }
        
        // Xử lý phần thưởng
        int coinWon = 0;
        if ("COIN".equals(wonReward.getRewardType()) && wonReward.getCoinAmount() != null) {
            coinWon = wonReward.getCoinAmount();
            
            // Cộng coin vào user
            user.setLoyaltyPoints(user.getLoyaltyPoints() + coinWon);
            userRepository.save(user);
            
            // Ghi transaction nhận coin
            LoyaltyTransaction earnTransaction = LoyaltyTransaction.builder()
                    .user(user)
                    .transactionType(LoyaltyTransaction.TransactionType.EARN)
                    .amount(coinWon)
                    .source("LUCKY_WHEEL")
                    .description("Trúng thưởng vòng quay: " + wonReward.getLabel())
                    .balanceAfter(user.getLoyaltyPoints())
                    .expiryDate(LocalDateTime.now().plusDays(30))
                    .build();
            loyaltyTransactionRepository.save(earnTransaction);
        }
        
        // Lưu lịch sử quay
        SpinHistory history = SpinHistory.builder()
                .user(user)
                .reward(wonReward)
                .rewardType(wonReward.getRewardType())
                .coinAmount(coinWon)
                .cost(cost)
                .build();
        spinHistoryRepository.save(history);
        
        // Kiểm tra còn lượt free không
        boolean hasFreeSpinLeft = !spinHistoryRepository.hasFreeSpinToday(userId);
        
        String message = "COIN".equals(wonReward.getRewardType()) 
                ? "Chúc mừng! Bạn nhận được " + coinWon + " coin" 
                : wonReward.getLabel();
        
        return SpinResponse.builder()
                .success(true)
                .message(message)
                .rewardPosition(wonReward.getPosition())
                .rewardType(wonReward.getRewardType())
                .coinAmount(coinWon)
                .totalPoints(user.getLoyaltyPoints())
                .hasFreeSpinLeft(hasFreeSpinLeft)
                .build();
    }

    /**
     * Chọn phần thưởng dựa trên xác suất
     */
    private LuckyWheelReward selectRewardByProbability() {
        List<LuckyWheelReward> rewards = rewardRepository.findAllByIsActiveTrueOrderByPositionAsc();
        
        if (rewards.isEmpty()) {
            return null;
        }
        
        // Tính tổng xác suất
        double totalProbability = rewards.stream()
                .mapToDouble(r -> r.getProbability().doubleValue())
                .sum();
        
        // Random một số từ 0 đến totalProbability
        double randomValue = random.nextDouble() * totalProbability;
        
        // Chọn reward dựa trên random value
        double cumulativeProbability = 0;
        for (LuckyWheelReward reward : rewards) {
            cumulativeProbability += reward.getProbability().doubleValue();
            if (randomValue <= cumulativeProbability) {
                log.info("🎰 Selected reward: {} (position: {}, probability: {}%)", 
                        reward.getLabel(), reward.getPosition(), reward.getProbability());
                return reward;
            }
        }
        
        // Fallback: trả về reward đầu tiên
        return rewards.get(0);
    }
}
