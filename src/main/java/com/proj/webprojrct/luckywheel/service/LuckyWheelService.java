package com.proj.webprojrct.luckywheel.service;

import com.proj.webprojrct.luckywheel.dto.LuckyWheelInfoResponse;
import com.proj.webprojrct.luckywheel.dto.SpinResponse;
import com.proj.webprojrct.luckywheel.entity.LuckyWheelConfig;
import com.proj.webprojrct.luckywheel.entity.LuckyWheelReward;
import com.proj.webprojrct.luckywheel.entity.ProductView;
import com.proj.webprojrct.luckywheel.entity.SpinHistory;
import com.proj.webprojrct.luckywheel.repository.LuckyWheelConfigRepository;
import com.proj.webprojrct.luckywheel.repository.LuckyWheelRewardRepository;
import com.proj.webprojrct.luckywheel.repository.ProductViewRepository;
import com.proj.webprojrct.luckywheel.repository.SpinHistoryRepository;
import com.proj.webprojrct.loyalty.entity.LoyaltyTransaction;
import com.proj.webprojrct.loyalty.repository.LoyaltyTransactionRepository;
import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.product.repository.ProductRepository;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final ProductViewRepository productViewRepository;
    private final ProductRepository productRepository;
    private final LuckyWheelConfigRepository configRepository;
    
    private final Random random = new Random();
    private static final int REQUIRED_PRODUCT_VIEWS = 3; // Yêu cầu xem 3 sản phẩm để có lượt quay
    private static final int MAX_DAILY_SPINS = 1; // Giới hạn 1 lượt quay mỗi ngày

    /**
     * Lấy thông tin vòng quay
     */
    public LuckyWheelInfoResponse getWheelInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Kiểm tra vòng quay có bật không
        boolean isEnabled = isLuckyWheelEnabled();
        if (!isEnabled) {
            return LuckyWheelInfoResponse.builder()
                    .hasFreeSpinToday(false)
                    .spinCost(0)
                    .currentPoints(user.getLoyaltyPoints())
                    .todaySpins(0L)
                    .totalCoinsWon(0)
                    .rewards(List.of())
                    .productsViewedToday(0L)
                    .requiredProductViews(REQUIRED_PRODUCT_VIEWS)
                    .wheelEnabled(false)
                    .build();
        }
        
        // Đếm số sản phẩm đã xem hôm nay
        long productsViewedToday = productViewRepository.countDistinctProductsViewedByUserAndDate(
                userId, LocalDate.now());
        
        // Kiểm tra đã quay hôm nay chưa
        long todaySpins = spinHistoryRepository.countTodaySpins(userId);
        boolean hasSpunToday = todaySpins >= MAX_DAILY_SPINS;
        
        // Kiểm tra đủ điều kiện quay (xem ít nhất 3 sản phẩm và chưa quay hôm nay)
        boolean canSpin = productsViewedToday >= REQUIRED_PRODUCT_VIEWS && !hasSpunToday;
        
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
                        .weight(r.getWeight())
                        .probability(r.getProbability())
                        .build())
                .collect(Collectors.toList());
        
        return LuckyWheelInfoResponse.builder()
                .hasFreeSpinToday(canSpin) // true nếu đủ điều kiện quay
                .spinCost(0) // Miễn phí
                .currentPoints(user.getLoyaltyPoints())
                .todaySpins(todaySpins)
                .totalCoinsWon(totalCoinsWon)
                .rewards(rewardItems)
                .productsViewedToday(productsViewedToday)
                .requiredProductViews(REQUIRED_PRODUCT_VIEWS)
                .wheelEnabled(true)
                .build();
    }

    /**
     * Thực hiện quay thưởng
     */
    @Transactional
    public SpinResponse performSpin(Long userId, Boolean useFreeSpin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Kiểm tra vòng quay có bật không
        if (!isLuckyWheelEnabled()) {
            return SpinResponse.builder()
                    .success(false)
                    .message("Vòng quay may mắn hiện đang tắt")
                    .build();
        }
        
        long todaySpins = spinHistoryRepository.countTodaySpins(userId);
        
        // Kiểm tra giới hạn 1 lượt quay mỗi ngày
        if (todaySpins >= MAX_DAILY_SPINS) {
            return SpinResponse.builder()
                    .success(false)
                    .message("Bạn đã hết lượt quay hôm nay (tối đa " + MAX_DAILY_SPINS + " lượt/ngày)")
                    .totalPoints(user.getLoyaltyPoints())
                    .build();
        }
        
        // Kiểm tra đã xem đủ 3 sản phẩm chưa
        long productsViewedToday = productViewRepository.countDistinctProductsViewedByUserAndDate(
                userId, LocalDate.now());
        
        if (productsViewedToday < REQUIRED_PRODUCT_VIEWS) {
            return SpinResponse.builder()
                    .success(false)
                    .message("Bạn cần xem chi tiết ít nhất " + REQUIRED_PRODUCT_VIEWS + 
                            " sản phẩm để có lượt quay (đã xem: " + productsViewedToday + ")")
                    .totalPoints(user.getLoyaltyPoints())
                    .build();
        }
        
        // Chọn phần thưởng dựa trên weight
        LuckyWheelReward wonReward = selectRewardByWeight();
        
        if (wonReward == null) {
            return SpinResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống vòng quay")
                    .build();
        }
        
        // Xử lý phần thưởng
        int coinWon = 0;
        String rewardType = wonReward.getRewardType();
        
        if (("COIN".equals(rewardType) || "COINS".equals(rewardType)) && wonReward.getCoinAmount() != null) {
            coinWon = wonReward.getCoinAmount();
            
            // Cộng coin vào user
            user.setLoyaltyPoints(user.getLoyaltyPoints() + coinWon);
            userRepository.save(user);
            
            // Ghi transaction nhận coin từ vòng quay
            LoyaltyTransaction earnTransaction = LoyaltyTransaction.builder()
                    .user(user)
                    .transactionType(LoyaltyTransaction.TransactionType.EARN)
                    .amount(coinWon)
                    .source("LUCKY_WHEEL")
                    .description("Trúng thưởng vòng quay: " + wonReward.getLabel())
                    .balanceAfter(user.getLoyaltyPoints())
                    .expiryDate(null) // Coin từ vòng quay không hết hạn
                    .build();
            loyaltyTransactionRepository.save(earnTransaction);
        }
        
        // Lưu lịch sử quay
        SpinHistory history = SpinHistory.builder()
                .user(user)
                .reward(wonReward)
                .rewardType(wonReward.getRewardType())
                .coinAmount(coinWon)
                .cost(0) // Miễn phí
                .build();
        spinHistoryRepository.save(history);
        
        // Tạo message
        String message;
        if (("COIN".equals(rewardType) || "COINS".equals(rewardType)) && coinWon > 0) {
            message = wonReward.getLabel(); // Dùng label từ admin (VD: "+100 Coin")
        } else {
            message = wonReward.getLabel(); // VD: "Chúc bạn may mắn lần sau"
        }
        
        return SpinResponse.builder()
                .success(true)
                .message(message)
                .rewardPosition(wonReward.getPosition())
                .rewardType(wonReward.getRewardType())
                .coinAmount(coinWon)
                .totalPoints(user.getLoyaltyPoints())
                .hasFreeSpinLeft(false) // Đã hết lượt
                .build();
    }

    /**
     * Chọn phần thưởng dựa trên weight (trọng số)
     */
    private LuckyWheelReward selectRewardByWeight() {
        List<LuckyWheelReward> rewards = rewardRepository.findAllByIsActiveTrueOrderByPositionAsc();
        
        if (rewards.isEmpty()) {
            return null;
        }
        
        // Tính tổng weight
        int totalWeight = rewards.stream()
                .mapToInt(LuckyWheelReward::getWeight)
                .sum();
        
        // Random một số từ 1 đến totalWeight
        int randomValue = random.nextInt(totalWeight) + 1;
        
        // Chọn reward dựa trên weight
        int cumulativeWeight = 0;
        for (LuckyWheelReward reward : rewards) {
            cumulativeWeight += reward.getWeight();
            if (randomValue <= cumulativeWeight) {
                log.info("🎰 Selected reward: {} (position: {}, weight: {}, probability: {}%)", 
                        reward.getLabel(), reward.getPosition(), reward.getWeight(), reward.getProbability());
                return reward;
            }
        }
        
        // Fallback: trả về reward đầu tiên
        return rewards.get(0);
    }
    
    /**
     * Ghi nhận lượt xem sản phẩm (để tích lũy lượt quay)
     */
    @Transactional
    public void recordProductView(Long userId, Long productId) {
        // Kiểm tra đã đủ 3 sản phẩm chưa (giới hạn tối đa)
        long currentViewedCount = productViewRepository.countDistinctProductsViewedByUserAndDate(
                userId, LocalDate.now());
        
        if (currentViewedCount >= REQUIRED_PRODUCT_VIEWS) {
            log.debug("User {} already viewed {} products today (max reached)", userId, currentViewedCount);
            return;
        }
        
        // Kiểm tra đã xem sản phẩm này hôm nay chưa
        boolean alreadyViewed = productViewRepository.existsByUserAndProductAndDate(
                userId, productId, LocalDate.now());
        
        if (alreadyViewed) {
            log.debug("User {} already viewed product {} today", userId, productId);
            return;
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        ProductView productView = ProductView.builder()
                .user(user)
                .product(product)
                .build();
        
        productViewRepository.save(productView);
        
        long viewedCount = productViewRepository.countDistinctProductsViewedByUserAndDate(
                userId, LocalDate.now());
        
        log.info("✅ User {} viewed product {} (total today: {}/{})", 
                userId, productId, viewedCount, REQUIRED_PRODUCT_VIEWS);
    }
    
    /**
     * Kiểm tra vòng quay có bật không
     */
    public boolean isLuckyWheelEnabled() {
        return configRepository.findByConfigKey("lucky_wheel_enabled")
                .map(config -> "true".equalsIgnoreCase(config.getConfigValue()))
                .orElse(true); // Mặc định bật
    }
}
