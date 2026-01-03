package com.proj.webprojrct.luckywheel.service;

import com.proj.webprojrct.luckywheel.entity.Prize;
import com.proj.webprojrct.luckywheel.entity.WheelConfig;
import com.proj.webprojrct.luckywheel.repository.PrizeRepository;
import com.proj.webprojrct.luckywheel.repository.WheelConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lucky Wheel Data Seeder
 * Automatically seeds initial prizes and configuration when application starts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LuckyWheelDataSeeder implements CommandLineRunner {

    private final PrizeRepository prizeRepository;
    private final WheelConfigRepository wheelConfigRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            seedWheelConfig();
            seedPrizes();
            log.info("✅ Lucky Wheel data seeded successfully!");
        } catch (Exception e) {
            log.error("❌ Failed to seed Lucky Wheel data", e);
        }
    }

    private void seedWheelConfig() {
        if (wheelConfigRepository.count() == 0) {
            WheelConfig config = new WheelConfig();
            config.setConfigKey("default");
            config.setMaxSpinsPerDay(3);
            config.setMaxSpinsPerWeek(21);
            config.setRequiresLogin(true);
            config.setRequiresOrder(false);
            config.setMinOrderCount(0);
            config.setIsActive(true);
            wheelConfigRepository.save(config);
            log.info("✓ Wheel config created");
        } else {
            log.info("✓ Wheel config already exists");
        }
    }

    private void seedPrizes() {
        if (prizeRepository.count() == 0) {
            // 1. Voucher 10%
            Prize prize1 = new Prize();
            prize1.setName("Giảm 10%");
            prize1.setType(Prize.PrizeType.VOUCHER);
            prize1.setDescription("Giảm 10% cho đơn hàng từ 500,000đ");
            prize1.setDiscountValue(10.0);
            prize1.setProbability(0.25); // 25%
            prize1.setQuantity(100);
            prize1.setRemainingQuantity(100);
            prize1.setIconUrl("/images/prizes/discount-10.png");
            prize1.setColor("#FF6B6B");
            prize1.setIsActive(true);
            prizeRepository.save(prize1);

            // 2. Voucher 20%
            Prize prize2 = new Prize();
            prize2.setName("Giảm 20%");
            prize2.setType(Prize.PrizeType.VOUCHER);
            prize2.setDescription("Giảm 20% cho đơn hàng từ 1,000,000đ");
            prize2.setDiscountValue(20.0);
            prize2.setProbability(0.15); // 15%
            prize2.setQuantity(50);
            prize2.setRemainingQuantity(50);
            prize2.setIconUrl("/images/prizes/discount-20.png");
            prize2.setColor("#4ECDC4");
            prize2.setIsActive(true);
            prizeRepository.save(prize2);

            // 3. Freeship
            Prize prize3 = new Prize();
            prize3.setName("Freeship");
            prize3.setType(Prize.PrizeType.FREESHIP);
            prize3.setDescription("Miễn phí vận chuyển cho mọi đơn hàng");
            prize3.setDiscountValue(0.0);
            prize3.setProbability(0.20); // 20%
            prize3.setQuantity(100);
            prize3.setRemainingQuantity(100);
            prize3.setIconUrl("/images/prizes/freeship.png");
            prize3.setColor("#FFE66D");
            prize3.setIsActive(true);
            prizeRepository.save(prize3);

            // 4. Points
            Prize prize4 = new Prize();
            prize4.setName("Điểm thưởng");
            prize4.setType(Prize.PrizeType.POINTS);
            prize4.setDescription("Nhận 100 điểm thưởng tích lũy");
            prize4.setPointsValue(100);
            prize4.setProbability(0.20); // 20%
            prize4.setQuantity(200);
            prize4.setRemainingQuantity(200);
            prize4.setIconUrl("/images/prizes/points.png");
            prize4.setColor("#95E1D3");
            prize4.setIsActive(true);
            prizeRepository.save(prize4);

            // 5. Gift
            Prize prize5 = new Prize();
            prize5.setName("Quà tặng");
            prize5.setType(Prize.PrizeType.VOUCHER);
            prize5.setDescription("Nhận 1 quà tặng bất ngờ từ Nike");
            prize5.setProbability(0.05); // 5%
            prize5.setQuantity(20);
            prize5.setRemainingQuantity(20);
            prize5.setIconUrl("/images/prizes/gift.png");
            prize5.setColor("#F38181");
            prize5.setIsActive(true);
            prizeRepository.save(prize5);

            // 6. Nothing
            Prize prize6 = new Prize();
            prize6.setName("Chúc may mắn");
            prize6.setType(Prize.PrizeType.NOTHING);
            prize6.setDescription("Chúc bạn may mắn lần sau!");
            prize6.setProbability(0.15); // 15%
            prize6.setQuantity(999);
            prize6.setRemainingQuantity(999);
            prize6.setIconUrl("/images/prizes/nothing.png");
            prize6.setColor("#AA96DA");
            prize6.setIsActive(true);
            prizeRepository.save(prize6);

            log.info("✓ Seeded 6 prizes with total probability: 100%");
        } else {
            log.info("✓ Prizes already exist (count: {})", prizeRepository.count());
        }
    }
}
