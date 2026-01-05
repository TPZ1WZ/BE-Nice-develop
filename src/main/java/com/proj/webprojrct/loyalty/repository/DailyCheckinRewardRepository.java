package com.proj.webprojrct.loyalty.repository;

import com.proj.webprojrct.loyalty.entity.DailyCheckinReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCheckinRewardRepository extends JpaRepository<DailyCheckinReward, Long> {

    /**
     * Lấy reward theo day number
     */
    Optional<DailyCheckinReward> findByDayNumber(Integer dayNumber);

    /**
     * Lấy tất cả rewards theo thứ tự ngày
     */
    List<DailyCheckinReward> findAllByOrderByDayNumberAsc();

    /**
     * Lấy các ngày bonus
     */
    List<DailyCheckinReward> findByIsBonusTrueOrderByDayNumberAsc();
}
