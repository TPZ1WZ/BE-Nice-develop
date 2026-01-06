package com.proj.webprojrct.luckywheel.repository;

import com.proj.webprojrct.luckywheel.entity.LuckyWheelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LuckyWheelConfigRepository extends JpaRepository<LuckyWheelConfig, Long> {
    
    Optional<LuckyWheelConfig> findByConfigKey(String configKey);
}
