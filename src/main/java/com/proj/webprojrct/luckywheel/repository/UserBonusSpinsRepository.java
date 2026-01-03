package com.proj.webprojrct.luckywheel.repository;

import com.proj.webprojrct.luckywheel.entity.UserBonusSpins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBonusSpinsRepository extends JpaRepository<UserBonusSpins, Long> {
    
    Optional<UserBonusSpins> findByUserId(Long userId);
    
    @Query("SELECT COALESCE(SUM(ubs.bonusSpins), 0) FROM UserBonusSpins ubs WHERE ubs.user.id = :userId")
    Integer getTotalBonusSpinsForUser(@Param("userId") Long userId);
}
