package com.proj.webprojrct.luckywheel.repository;

import com.proj.webprojrct.luckywheel.entity.WheelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WheelConfigRepository extends JpaRepository<WheelConfig, Long> {
    
    Optional<WheelConfig> findByConfigKey(String configKey);
}
