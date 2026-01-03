package com.proj.webprojrct.luckywheel.service;

import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.luckywheel.dto.BatchProbabilityRequest;
import com.proj.webprojrct.luckywheel.dto.LuckyWheelStatistics;
import com.proj.webprojrct.luckywheel.dto.PrizeProbabilityUpdate;
import com.proj.webprojrct.luckywheel.dto.UserSpinManagement;
import com.proj.webprojrct.luckywheel.entity.Prize;
import com.proj.webprojrct.luckywheel.entity.SpinHistory;
import com.proj.webprojrct.luckywheel.entity.UserCoupon;
import com.proj.webprojrct.luckywheel.entity.WheelConfig;
import com.proj.webprojrct.luckywheel.entity.UserBonusSpins;
import com.proj.webprojrct.luckywheel.repository.PrizeRepository;
import com.proj.webprojrct.luckywheel.repository.SpinHistoryRepository;
import com.proj.webprojrct.luckywheel.repository.UserBonusSpinsRepository;
import com.proj.webprojrct.luckywheel.repository.UserCouponRepository;
import com.proj.webprojrct.luckywheel.repository.WheelConfigRepository;
import com.proj.webprojrct.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LuckyWheelService {

    private final PrizeRepository prizeRepository;
    private final SpinHistoryRepository spinHistoryRepository;
    private final WheelConfigRepository wheelConfigRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserBonusSpinsRepository userBonusSpinsRepository;
    private final Random random = new Random();

    private static final String DEFAULT_CONFIG_KEY = "default";

    /**
     * Lấy cấu hình vòng quay
     */
    public WheelConfig getWheelConfig() {
        return wheelConfigRepository.findByConfigKey(DEFAULT_CONFIG_KEY)
                .orElseGet(this::createDefaultConfig);
    }

    /**
     * Tạo cấu hình mặc định
     */
    private WheelConfig createDefaultConfig() {
        WheelConfig config = new WheelConfig();
        config.setConfigKey(DEFAULT_CONFIG_KEY);
        config.setIsActive(true);
        config.setMaxSpinsPerDay(1);
        config.setMaxSpinsPerWeek(7);
        config.setRequiresLogin(true);
        config.setRequiresOrder(false);
        config.setDescription("Chúc bạn may mắn!");
        return wheelConfigRepository.save(config);
    }

    /**
     * Lấy danh sách phần thưởng hiện có
     */
    public List<Prize> getAvailablePrizes() {
        return prizeRepository.findAvailablePrizes();
    }

    /**
     * Quay thưởng cho user
     */
    @Transactional
    public SpinHistory spin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        WheelConfig config = getWheelConfig();

        // Kiểm tra vòng quay có hoạt động không
        if (!config.getIsActive()) {
            throw new RuntimeException("Vòng quay hiện không hoạt động");
        }
        
        // Kiểm tra thời gian sự kiện
        if (!config.isCurrentlyActive()) {
            throw new RuntimeException(config.getStatusMessage());
        }

        // Kiểm tra số lần quay trong ngày
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        Long spinsToday = spinHistoryRepository.countUserSpinsSince(userId, startOfDay);
        Integer bonusSpins = userBonusSpinsRepository.getTotalBonusSpinsForUser(userId);
        
        int maxSpinsAllowed = config.getMaxSpinsPerDay() + bonusSpins;
        if (spinsToday >= maxSpinsAllowed) {
            throw new RuntimeException("Bạn đã hết lượt quay hôm nay");
        }

        // Lấy danh sách phần thưởng có thể trúng
        List<Prize> availablePrizes = getAvailablePrizes();
        if (availablePrizes.isEmpty()) {
            throw new RuntimeException("Không có phần thưởng nào");
        }

        // Random phần thưởng dựa trên probability
        Prize wonPrize = selectRandomPrize(availablePrizes);

        // Giảm số lượng phần thưởng
        if (wonPrize.getQuantity() != null) {
            wonPrize.decreaseQuantity();
            prizeRepository.save(wonPrize);
        }

        // Tạo mã code nếu là voucher/freeship
        String prizeCode = null;
        if (wonPrize.getType() == Prize.PrizeType.VOUCHER || 
            wonPrize.getType() == Prize.PrizeType.FREESHIP) {
            prizeCode = generatePrizeCode(wonPrize.getType());
        }

        // Lưu lịch sử quay
        SpinHistory history = new SpinHistory();
        history.setUser(user);
        history.setPrize(wonPrize);
        history.setPrizeCode(prizeCode);
        history.setSpinTime(LocalDateTime.now());
        history.setIsClaimed(false);

        SpinHistory savedHistory = spinHistoryRepository.save(history);
        
        // Trừ bonus spins nếu đã vượt quá lượt quay thường
        if (spinsToday >= config.getMaxSpinsPerDay() && bonusSpins > 0) {
            decreaseBonusSpins(userId, 1);
        }
        
        return savedHistory;
    }

    /**
     * Random phần thưởng dựa trên xác suất
     */
    private Prize selectRandomPrize(List<Prize> prizes) {
        // Tính tổng xác suất
        double totalProbability = prizes.stream()
                .mapToDouble(Prize::getProbability)
                .sum();

        // Random số từ 0 đến totalProbability
        double randomValue = random.nextDouble() * totalProbability;

        // Chọn phần thưởng
        double cumulativeProbability = 0;
        for (Prize prize : prizes) {
            cumulativeProbability += prize.getProbability();
            if (randomValue <= cumulativeProbability) {
                return prize;
            }
        }

        // Fallback: return phần thưởng cuối cùng
        return prizes.get(prizes.size() - 1);
    }

    /**
     * Tạo mã code cho phần thưởng
     */
    private String generatePrizeCode(Prize.PrizeType type) {
        String prefix = type == Prize.PrizeType.VOUCHER ? "VC" : "FS";
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Lấy lịch sử quay của user
     */
    public Page<SpinHistory> getUserSpinHistory(Long userId, Pageable pageable) {
        return spinHistoryRepository.findByUserIdOrderBySpinTimeDesc(userId, pageable);
    }

    /**
     * Lấy số lần quay còn lại trong ngày
     */
    public Integer getRemainingSpinsToday(Long userId) {
        WheelConfig config = getWheelConfig();
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        Long spinsToday = spinHistoryRepository.countUserSpinsSince(userId, startOfDay);
        Integer bonusSpins = userBonusSpinsRepository.getTotalBonusSpinsForUser(userId);
        int maxSpinsAllowed = config.getMaxSpinsPerDay() + bonusSpins;
        return Math.max(0, maxSpinsAllowed - spinsToday.intValue());
    }

    /**
     * Đánh dấu đã nhận thưởng
     */
    @Transactional
    public SpinHistory claimPrize(Long historyId, Long userId) {
        SpinHistory history = spinHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Lịch sử quay không tồn tại"));

        if (!history.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền");
        }

        if (history.getIsClaimed()) {
            throw new RuntimeException("Đã nhận thưởng rồi");
        }

        history.setIsClaimed(true);
        history.setClaimedTime(LocalDateTime.now());
        return spinHistoryRepository.save(history);
    }

    // ============== ADMIN APIs ==============

    /**
     * Tạo phần thưởng mới
     */
    @Transactional
    public Prize createPrize(Prize prize) {
        if (prize.getRemainingQuantity() == null && prize.getQuantity() != null) {
            prize.setRemainingQuantity(prize.getQuantity());
        }
        return prizeRepository.save(prize);
    }

    /**
     * Cập nhật phần thưởng
     */
    @Transactional
    public Prize updatePrize(Long prizeId, Prize updatedPrize) {
        Prize prize = prizeRepository.findById(prizeId)
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));

        prize.setName(updatedPrize.getName());
        prize.setType(updatedPrize.getType());
        prize.setDescription(updatedPrize.getDescription());
        prize.setDiscountValue(updatedPrize.getDiscountValue());
        prize.setPointsValue(updatedPrize.getPointsValue());
        prize.setProbability(updatedPrize.getProbability());
        prize.setQuantity(updatedPrize.getQuantity());
        prize.setRemainingQuantity(updatedPrize.getRemainingQuantity());
        prize.setIsActive(updatedPrize.getIsActive());
        prize.setIconUrl(updatedPrize.getIconUrl());
        prize.setColor(updatedPrize.getColor());

        return prizeRepository.save(prize);
    }

    /**
     * Xóa phần thưởng
     */
    @Transactional
    public void deletePrize(Long prizeId) {
        prizeRepository.deleteById(prizeId);
    }

    /**
     * Lấy tất cả phần thưởng (admin)
     */
    public List<Prize> getAllPrizes() {
        return prizeRepository.findAll();
    }

    /**
     * Cập nhật cấu hình vòng quay
     */
    @Transactional
    public WheelConfig updateConfig(WheelConfig config) {
        WheelConfig existingConfig = getWheelConfig();
        existingConfig.setIsActive(config.getIsActive());
        existingConfig.setMaxSpinsPerDay(config.getMaxSpinsPerDay());
        existingConfig.setMaxSpinsPerWeek(config.getMaxSpinsPerWeek());
        existingConfig.setRequiresLogin(config.getRequiresLogin());
        existingConfig.setRequiresOrder(config.getRequiresOrder());
        existingConfig.setMinOrderCount(config.getMinOrderCount());
        existingConfig.setDescription(config.getDescription());
        return wheelConfigRepository.save(existingConfig);
    }

    /**
     * Xem tất cả lịch sử quay (admin)
     */
    public Page<SpinHistory> getAllSpinHistory(Pageable pageable) {
        return spinHistoryRepository.findAllSpins(pageable);
    }

    /**
     * Lấy danh sách phần thưởng đã nhận của user
     */
    public List<SpinHistory> getUserWonPrizes(Long userId) {
        return spinHistoryRepository.findByUserIdOrderBySpinTimeDesc(userId, Pageable.unpaged()).getContent();
    }

    /**
     * Lấy danh sách phần thưởng hấp dẫn (phần thưởng có giá trị cao và còn nhiều)
     * Sắp xếp theo: discountValue giảm dần, remainingQuantity giảm dần
     */
    public List<Prize> getAttractivePrizes() {
        return prizeRepository.findAttractivePrizes();
    }

    /**
     * Lưu voucher vào ví của user
     */
    @Transactional
    public UserCoupon saveToWallet(Long historyId, Long userId) {
        SpinHistory history = spinHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Lịch sử quay không tồn tại"));

        // Kiểm tra quyền
        if (!history.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền");
        }

        // Kiểm tra đã lưu vào ví chưa
        if (userCouponRepository.existsBySpinHistoryIdAndUserId(historyId, userId)) {
            throw new RuntimeException("Voucher này đã được lưu vào ví rồi");
        }

        // Chỉ lưu voucher và freeship
        Prize prize = history.getPrize();
        if (prize.getType() != Prize.PrizeType.VOUCHER && 
            prize.getType() != Prize.PrizeType.FREESHIP) {
            throw new RuntimeException("Chỉ có thể lưu voucher/freeship vào ví");
        }

        // Tạo UserCoupon mới
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUser(user);
        userCoupon.setSpinHistory(history);
        userCoupon.setCouponCode(history.getPrizeCode());
        userCoupon.setPrizeName(prize.getName());
        userCoupon.setPrizeDescription(prize.getDescription());
        userCoupon.setDiscountValue(prize.getDiscountValue());
        userCoupon.setPrizeType(prize.getType());
        userCoupon.setIsUsed(false);
        // expiresAt sẽ được set trong @PrePersist (30 ngày)

        return userCouponRepository.save(userCoupon);
    }

    /**
     * Lấy danh sách voucher trong ví (còn hợp lệ)
     */
    public List<UserCoupon> getMyWallet(Long userId) {
        return userCouponRepository.findValidCouponsByUserId(userId, LocalDateTime.now());
    }

    /**
     * Lấy tất cả voucher của user (cả đã dùng và hết hạn)
     */
    public List<UserCoupon> getAllMyVouchers(Long userId) {
        return userCouponRepository.findByUserIdOrderBySavedAtDesc(userId);
    }

    // ============== ADMIN APIs ==============

    /**
     * Cập nhật xác suất nhiều phần thưởng cùng lúc
     */
    @Transactional
    public List<Prize> batchUpdateProbabilities(BatchProbabilityRequest request) {
        // Validate tổng xác suất phải bằng 1.0 (100%)
        double totalProbability = request.getPrizes().stream()
                .mapToDouble(PrizeProbabilityUpdate::getProbability)
                .sum();
        
        // Cho phép sai số nhỏ (0.01 = 1%) do floating point
        if (Math.abs(totalProbability - 1.0) > 0.01) {
            throw new RuntimeException(
                String.format("Tổng xác suất phải bằng 100%%. Hiện tại: %.0f%%", 
                    totalProbability * 100)
            );
        }
        
        List<Prize> updatedPrizes = new ArrayList<>();
        
        for (PrizeProbabilityUpdate update : request.getPrizes()) {
            Prize prize = prizeRepository.findById(update.getPrizeId())
                    .orElseThrow(() -> new RuntimeException("Phần thưởng ID " + update.getPrizeId() + " không tồn tại"));
            
            prize.setProbability(update.getProbability());
            updatedPrizes.add(prizeRepository.save(prize));
        }
        
        return updatedPrizes;
    }

    /**
     * Lấy danh sách user và lượt quay của họ
     */
    public List<UserSpinManagement> getUserSpinManagement() {
        List<User> users = userRepository.findAll();
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime startOfWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        
        return users.stream().map(user -> {
            Long spinsToday = spinHistoryRepository.countUserSpinsSince(user.getId(), startOfDay);
            Long spinsThisWeek = spinHistoryRepository.countUserSpinsSince(user.getId(), startOfWeek);
            Long totalSpins = spinHistoryRepository.countByUserId(user.getId());
            Long prizesWon = spinHistoryRepository.countPrizesWonByUserId(user.getId());
            
            UserSpinManagement management = new UserSpinManagement();
            management.setUserId(user.getId());
            management.setUsername(user.getUsername());
            management.setEmail(user.getEmail());
            management.setSpinsToday(spinsToday.intValue());
            management.setSpinsThisWeek(spinsThisWeek.intValue());
            management.setTotalSpins(totalSpins.intValue());
            management.setPrizesWon(prizesWon.intValue());
            
            return management;
        }).collect(Collectors.toList());
    }

    /**
     * Reset lượt quay của user (xóa lịch sử hôm nay)
     * Chỉ nên dùng cho testing hoặc trường hợp đặc biệt
     */
    @Transactional
    public void resetUserSpinsToday(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        spinHistoryRepository.deleteUserSpinsSince(userId, startOfDay);
    }

    /**
     * Tặng thêm lượt quay cho user
     */
    @Transactional
    public void grantBonusSpins(Long userId, Integer bonusSpins) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        UserBonusSpins userBonus = userBonusSpinsRepository.findByUserId(userId)
                .orElse(new UserBonusSpins());
        
        if (userBonus.getId() == null) {
            userBonus.setUser(user);
            userBonus.setBonusSpins(bonusSpins);
        } else {
            userBonus.setBonusSpins(userBonus.getBonusSpins() + bonusSpins);
        }
        
        userBonus.setGrantedAt(LocalDateTime.now());
        userBonus.setGrantedBy("ADMIN");
        userBonus.setReason("Admin granted bonus spins");
        
        userBonusSpinsRepository.save(userBonus);
        log.info("Granted {} bonus spins to user {}. Total bonus spins: {}", 
                bonusSpins, userId, userBonus.getBonusSpins());
    }
    
    /**
     * Trừ lượt quay bonus
     */
    @Transactional
    public void decreaseBonusSpins(Long userId, Integer amount) {
        userBonusSpinsRepository.findByUserId(userId).ifPresent(userBonus -> {
            int newAmount = Math.max(0, userBonus.getBonusSpins() - amount);
            userBonus.setBonusSpins(newAmount);
            userBonusSpinsRepository.save(userBonus);
            log.info("Decreased {} bonus spins for user {}. Remaining: {}", 
                    amount, userId, newAmount);
        });
    }

    /**
     * Thống kê vòng quay cho Admin
     */
    public LuckyWheelStatistics getStatistics() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime startOfWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        
        LuckyWheelStatistics stats = new LuckyWheelStatistics();
        
        // Tổng số lượt quay
        stats.setTotalSpins(spinHistoryRepository.count());
        stats.setSpinsToday(spinHistoryRepository.countSpinsSince(startOfDay));
        stats.setSpinsThisWeek(spinHistoryRepository.countSpinsSince(startOfWeek));
        
        // Số user đã quay
        stats.setUniqueUsers(spinHistoryRepository.countUniqueUsers());
        
        // Tổng phần thưởng đã phát
        stats.setTotalPrizesWon(spinHistoryRepository.countPrizesWon());
        
        // Phần thưởng phổ biến nhất
        Object[] mostPopular = spinHistoryRepository.findMostPopularPrize();
        if (mostPopular != null && mostPopular.length >= 2) {
            stats.setMostPopularPrize((String) mostPopular[0]);
            stats.setMostPopularPrizeCount(((Number) mostPopular[1]).longValue());
        }
        
        // User quay nhiều nhất
        Object[] topSpinner = spinHistoryRepository.findTopSpinner();
        if (topSpinner != null && topSpinner.length >= 2) {
            stats.setTopSpinner((String) topSpinner[0]);
            stats.setTopSpinnerCount(((Number) topSpinner[1]).longValue());
        }
        
        return stats;
    }

    /**
     * Bật/tắt vòng quay
     */
    @Transactional
    public WheelConfig toggleWheelStatus(Boolean isActive) {
        WheelConfig config = getWheelConfig();
        config.setIsActive(isActive);
        return wheelConfigRepository.save(config);
    }

    /**
     * Cập nhật thời gian sự kiện
     */
    @Transactional
    public WheelConfig updateEventSchedule(String eventName, LocalDateTime startDate, LocalDateTime endDate, Boolean isTimeRestricted) {
        WheelConfig config = getWheelConfig();
        config.setEventName(eventName);
        config.setStartDate(startDate);
        config.setEndDate(endDate);
        config.setIsTimeRestricted(isTimeRestricted);
        return wheelConfigRepository.save(config);
    }
}
