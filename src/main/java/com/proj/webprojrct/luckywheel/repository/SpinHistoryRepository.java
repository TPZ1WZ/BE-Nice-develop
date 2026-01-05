package com.proj.webprojrct.luckywheel.repository;

import com.proj.webprojrct.luckywheel.entity.SpinHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {
    
    // Kiểm tra đã quay miễn phí hôm nay chưa
    @Query("SELECT COUNT(s) > 0 FROM SpinHistory s " +
           "WHERE s.user.id = :userId " +
           "AND CAST(s.spinDate AS date) = CURRENT_DATE " +
           "AND s.cost = 0")
    boolean hasFreeSpinToday(@Param("userId") Long userId);
    
    // Lấy lịch sử quay của user
    List<SpinHistory> findByUserIdOrderBySpinDateDesc(Long userId);
    
    // Đếm số lần quay hôm nay
    @Query("SELECT COUNT(s) FROM SpinHistory s " +
           "WHERE s.user.id = :userId " +
           "AND CAST(s.spinDate AS date) = CURRENT_DATE")
    long countTodaySpins(@Param("userId") Long userId);
    
    // Tổng coin đã thắng
    @Query("SELECT COALESCE(SUM(s.coinAmount), 0) FROM SpinHistory s " +
           "WHERE s.user.id = :userId")
    int getTotalCoinsWon(@Param("userId") Long userId);
}
