package com.proj.webprojrct.luckywheel.repository;

import com.proj.webprojrct.luckywheel.entity.LuckyWheelReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LuckyWheelRewardRepository extends JpaRepository<LuckyWheelReward, Long> {
    
    List<LuckyWheelReward> findAllByIsActiveTrueOrderByPositionAsc();
    
    @Query("SELECT SUM(r.probability) FROM LuckyWheelReward r WHERE r.isActive = true")
    Double getTotalProbability();
}
