package com.proj.webprojrct.loyalty.service;

import com.proj.webprojrct.loyalty.dto.CheckinResponse;
import com.proj.webprojrct.loyalty.dto.CheckinStreakResponse;
import com.proj.webprojrct.loyalty.dto.LoyaltyPointsResponse;
import com.proj.webprojrct.loyalty.entity.DailyCheckin;
import com.proj.webprojrct.loyalty.entity.DailyCheckinReward;
import com.proj.webprojrct.loyalty.entity.LoyaltyTransaction;
import com.proj.webprojrct.loyalty.repository.DailyCheckinRepository;
import com.proj.webprojrct.loyalty.repository.DailyCheckinRewardRepository;
import com.proj.webprojrct.loyalty.repository.LoyaltyTransactionRepository;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final DailyCheckinRepository dailyCheckinRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final DailyCheckinRewardRepository dailyCheckinRewardRepository;
    private final UserRepository userRepository;

    /**
     * Lấy thông tin streak và rewards của user
     */
    public CheckinStreakResponse getCheckinStreak(Long userId) {
        // Lấy streak hiện tại
        int currentStreak = calculateCurrentStreak(userId);
        
        // Kiểm tra đã checkin hôm nay chưa
        boolean hasCheckedInToday = dailyCheckinRepository
                .existsByUserIdAndCheckinDate(userId, LocalDate.now());
        
        // Lấy ngày checkin gần nhất
        LocalDate lastCheckinDate = dailyCheckinRepository
                .findTopByUserIdOrderByCheckinDateDesc(userId)
                .map(DailyCheckin::getCheckinDate)
                .orElse(null);
        
        // Tổng số lần checkin
        long totalCheckins = dailyCheckinRepository.countByUserId(userId);
        
        // Lấy thông tin rewards cho 7 ngày
        List<DailyCheckinReward> rewards = dailyCheckinRewardRepository
                .findAllByOrderByDayNumberAsc();
        
        List<CheckinStreakResponse.DayRewardInfo> weeklyRewards = new ArrayList<>();
        for (DailyCheckinReward reward : rewards) {
            CheckinStreakResponse.CheckinStatus status;
            
            if (reward.getDayNumber() - 1 < currentStreak) {
                status = CheckinStreakResponse.CheckinStatus.PAST;
            } else if (reward.getDayNumber() - 1 == currentStreak && !hasCheckedInToday) {
                status = CheckinStreakResponse.CheckinStatus.TODAY;
            } else {
                status = CheckinStreakResponse.CheckinStatus.FUTURE;
            }
            
            weeklyRewards.add(CheckinStreakResponse.DayRewardInfo.builder()
                    .dayNumber(reward.getDayNumber())
                    .rewardAmount(reward.getRewardAmount())
                    .isBonus(reward.getIsBonus())
                    .status(status)
                    .build());
        }
        
        // Phần thưởng hôm nay (nếu chưa claim)
        Integer todayReward = null;
        if (!hasCheckedInToday && currentStreak < 7) {
            int todayDayNumber = (currentStreak % 7) + 1;
            todayReward = dailyCheckinRewardRepository
                    .findByDayNumber(todayDayNumber)
                    .map(DailyCheckinReward::getRewardAmount)
                    .orElse(1000);
        }
        
        return CheckinStreakResponse.builder()
                .currentStreak(currentStreak)
                .hasCheckedInToday(hasCheckedInToday)
                .todayReward(todayReward)
                .lastCheckinDate(lastCheckinDate)
                .totalCheckins(totalCheckins)
                .weeklyRewards(weeklyRewards)
                .build();
    }

    /**
     * Thực hiện checkin hàng ngày
     */
    @Transactional
    public CheckinResponse performCheckin(Long userId) {
        // Kiểm tra đã checkin hôm nay chưa
        if (dailyCheckinRepository.existsByUserIdAndCheckinDate(userId, LocalDate.now())) {
            return CheckinResponse.builder()
                    .success(false)
                    .message("Bạn đã checkin hôm nay rồi")
                    .build();
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Tính streak hiện tại
        int currentStreak = calculateCurrentStreak(userId);
        
        // Nếu hôm qua không checkin và streak > 0 → reset về 0
        LocalDate yesterday = LocalDate.now().minusDays(1);
        boolean checkedInYesterday = dailyCheckinRepository
                .existsByUserIdAndCheckinDate(userId, yesterday);
        
        if (!checkedInYesterday && currentStreak > 0) {
            // Kiểm tra xem có phải lần checkin đầu tiên không
            long totalCheckins = dailyCheckinRepository.countByUserId(userId);
            if (totalCheckins > 0) {
                currentStreak = 0; // Reset streak
            }
        }
        
        // Streak mới (tăng 1, max 7, sau 7 reset về 1)
        int newStreak = (currentStreak % 7) + 1;
        
        // Lấy reward theo day number
        DailyCheckinReward reward = dailyCheckinRewardRepository
                .findByDayNumber(newStreak)
                .orElseThrow(() -> new RuntimeException("Reward configuration not found"));
        
        // Tạo checkin record
        DailyCheckin checkin = DailyCheckin.builder()
                .user(user)
                .checkinDate(LocalDate.now())
                .rewardAmount(reward.getRewardAmount())
                .currentStreak(newStreak)
                .build();
        
        dailyCheckinRepository.save(checkin);
        
        // Tạo loyalty transaction
        int currentPoints = user.getLoyaltyPoints();
        int newPoints = currentPoints + reward.getRewardAmount();
        
        // Set expiry date = 30 ngày sau
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);
        
        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .user(user)
                .transactionType(LoyaltyTransaction.TransactionType.EARN)
                .amount(reward.getRewardAmount())
                .source("DAILY_CHECKIN")
                .description("Daily checkin Day " + newStreak + " reward")
                .balanceAfter(newPoints)
                .expiryDate(expiryDate)
                .build();
        
        LoyaltyTransaction savedTransaction = loyaltyTransactionRepository.save(transaction);
        
        // Cập nhật loyalty points của user (trigger sẽ tự động update)
        // Nhưng để đảm bảo, ta update luôn
        user.setLoyaltyPoints(newPoints);
        userRepository.save(user);
        
        log.info("User {} checked in. Day {}, Reward: {}, Total points: {}", 
                userId, newStreak, reward.getRewardAmount(), newPoints);
        
        return CheckinResponse.builder()
                .success(true)
                .message("Checkin thành công!")
                .rewardAmount(reward.getRewardAmount())
                .currentStreak(newStreak)
                .totalPoints(newPoints)
                .transactionId(savedTransaction.getId())
                .build();
    }

    /**
     * Lấy thông tin loyalty points của user
     */
    public LoyaltyPointsResponse getLoyaltyPoints(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        int currentPoints = user.getLoyaltyPoints();
        
        Integer totalEarned = loyaltyTransactionRepository.getTotalEarnedByUserId(userId);
        Integer totalSpent = loyaltyTransactionRepository.getTotalSpentByUserId(userId);
        
        int currentStreak = calculateCurrentStreak(userId);
        long totalCheckins = dailyCheckinRepository.countByUserId(userId);
        
        // Lấy thông tin coin sắp hết hạn sớm nhất
        List<LoyaltyTransaction> validTransactions = loyaltyTransactionRepository
                .findValidEarnTransactionsByUserIdOrderByExpiryAsc(userId);
        
        Integer expiringCoins = null;
        LocalDateTime expiryDate = null;
        
        if (!validTransactions.isEmpty()) {
            // Lấy ngày hết hạn sớm nhất
            expiryDate = validTransactions.get(0).getExpiryDate();
            
            // Tính tổng coin có cùng ngày hết hạn đó
            LocalDateTime finalExpiryDate = expiryDate;
            expiringCoins = validTransactions.stream()
                    .filter(t -> t.getExpiryDate().equals(finalExpiryDate))
                    .mapToInt(LoyaltyTransaction::getAmount)
                    .sum();
        }
        
        return LoyaltyPointsResponse.builder()
                .currentPoints(currentPoints)
                .totalEarned(totalEarned != null ? totalEarned : 0)
                .totalSpent(totalSpent != null ? totalSpent : 0)
                .currentStreak(currentStreak)
                .totalCheckins(totalCheckins)
                .expiringCoins(expiringCoins)
                .expiryDate(expiryDate)
                .build();
    }

    /**
     * Tính current streak của user
     */
    private int calculateCurrentStreak(Long userId) {
        Optional<DailyCheckin> lastCheckin = dailyCheckinRepository
                .findTopByUserIdOrderByCheckinDateDesc(userId);
        
        if (lastCheckin.isEmpty()) {
            return 0;
        }
        
        LocalDate lastDate = lastCheckin.get().getCheckinDate();
        LocalDate today = LocalDate.now();
        
        // Nếu ngày cuối cùng checkin là hôm nay hoặc hôm qua
        if (lastDate.equals(today) || lastDate.equals(today.minusDays(1))) {
            return lastCheckin.get().getCurrentStreak();
        }
        
        // Nếu lâu hơn → streak reset về 0
        return 0;
    }

    /**
     * Thêm points cho user (dùng cho admin hoặc các tính năng khác)
     */
    @Transactional
    public void addPoints(Long userId, Integer amount, String source, String description, Long referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        int currentPoints = user.getLoyaltyPoints();
        int newPoints = currentPoints + amount;
        
        // Set expiry date = 30 ngày sau
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);
        
        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .user(user)
                .transactionType(LoyaltyTransaction.TransactionType.EARN)
                .amount(amount)
                .source(source)
                .description(description)
                .referenceId(referenceId)
                .balanceAfter(newPoints)
                .expiryDate(expiryDate)
                .build();
        
        loyaltyTransactionRepository.save(transaction);
        
        user.setLoyaltyPoints(newPoints);
        userRepository.save(user);
        
        log.info("Added {} points to user {}. New balance: {}", amount, userId, newPoints);
    }

    /**
     * Trừ points của user
     */
    @Transactional
    public void deductPoints(Long userId, Integer amount, String source, String description, Long referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        int currentPoints = user.getLoyaltyPoints();
        
        if (currentPoints < amount) {
            throw new RuntimeException("Không đủ điểm. Hiện có: " + currentPoints);
        }
        
        int newPoints = currentPoints - amount;
        
        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .user(user)
                .transactionType(LoyaltyTransaction.TransactionType.SPEND)
                .amount(-amount) // Số âm
                .source(source)
                .description(description)
                .referenceId(referenceId)
                .balanceAfter(newPoints)
                .build();
        
        loyaltyTransactionRepository.save(transaction);
        
        user.setLoyaltyPoints(newPoints);
        userRepository.save(user);
        
        log.info("Deducted {} points from user {}. New balance: {}", amount, userId, newPoints);
    }
    
    /**
     * Lấy lịch sử giao dịch của user (giới hạn 50 giao dịch gần nhất)
     */
    public List<com.proj.webprojrct.loyalty.dto.TransactionHistoryResponse> getTransactionHistory(Long userId) {
        List<LoyaltyTransaction> transactions = loyaltyTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // Giới hạn 50 giao dịch gần nhất
        if (transactions.size() > 50) {
            transactions = transactions.subList(0, 50);
        }
        
        // Convert sang DTO
        return transactions.stream()
                .map(com.proj.webprojrct.loyalty.dto.TransactionHistoryResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
