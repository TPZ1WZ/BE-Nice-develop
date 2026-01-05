package com.proj.webprojrct.loyalty.repository;

import com.proj.webprojrct.loyalty.entity.DailyCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCheckinRepository extends JpaRepository<DailyCheckin, Long> {

    /**
     * Kiểm tra user đã checkin hôm nay chưa
     */
    boolean existsByUserIdAndCheckinDate(Long userId, LocalDate date);

    /**
     * Lấy checkin gần nhất của user
     */
    Optional<DailyCheckin> findTopByUserIdOrderByCheckinDateDesc(Long userId);

    /**
     * Lấy lịch sử checkin của user (7 ngày gần nhất)
     */
    List<DailyCheckin> findTop7ByUserIdOrderByCheckinDateDesc(Long userId);

    /**
     * Lấy tất cả checkin của user
     */
    List<DailyCheckin> findByUserIdOrderByCheckinDateDesc(Long userId);

    /**
     * Đếm số ngày đã checkin của user
     */
    long countByUserId(Long userId);

    /**
     * Lấy checkin của user trong khoảng thời gian
     */
    @Query("SELECT dc FROM DailyCheckin dc WHERE dc.user.id = :userId " +
           "AND dc.checkinDate BETWEEN :startDate AND :endDate " +
           "ORDER BY dc.checkinDate DESC")
    List<DailyCheckin> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
