package com.proj.webprojrct.luckywheel.repository;

import com.proj.webprojrct.luckywheel.entity.SpinHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {
    
    Page<SpinHistory> findByUserIdOrderBySpinTimeDesc(Long userId, Pageable pageable);
    
    @Query("SELECT COUNT(s) FROM SpinHistory s WHERE s.user.id = :userId AND s.spinTime >= :startTime")
    Long countUserSpinsSince(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
    
    List<SpinHistory> findByUserIdAndIsClaimedFalse(Long userId);
    
    @Query("SELECT s FROM SpinHistory s ORDER BY s.spinTime DESC")
    Page<SpinHistory> findAllSpins(Pageable pageable);
    
    // ==== Admin queries ====
    
    Long countByUserId(Long userId);
    
    @Query("SELECT COUNT(s) FROM SpinHistory s WHERE s.user.id = :userId AND s.prize.type != 'NOTHING'")
    Long countPrizesWonByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(s) FROM SpinHistory s WHERE s.spinTime >= :startTime")
    Long countSpinsSince(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(DISTINCT s.user.id) FROM SpinHistory s")
    Long countUniqueUsers();
    
    @Query("SELECT COUNT(s) FROM SpinHistory s WHERE s.prize.type != 'NOTHING'")
    Long countPrizesWon();
    
    @Query("SELECT p.name, COUNT(s) FROM SpinHistory s JOIN s.prize p WHERE p.type != 'NOTHING' GROUP BY p.name ORDER BY COUNT(s) DESC")
    List<Object[]> findMostPopularPrizes();
    
    @Query("SELECT p.name, COUNT(s) FROM SpinHistory s JOIN s.prize p WHERE p.type != 'NOTHING' GROUP BY p.name ORDER BY COUNT(s) DESC")
    Object[] findMostPopularPrize();
    
    @Query("SELECT u.fullName, COUNT(s) FROM SpinHistory s JOIN s.user u GROUP BY u.fullName ORDER BY COUNT(s) DESC")
    List<Object[]> findTopSpinners();
    
    @Query("SELECT u.fullName, COUNT(s) FROM SpinHistory s JOIN s.user u GROUP BY u.fullName ORDER BY COUNT(s) DESC")
    Object[] findTopSpinner();
    
    @Modifying
    @Query("DELETE FROM SpinHistory s WHERE s.user.id = :userId AND s.spinTime >= :startTime")
    void deleteUserSpinsSince(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
}
