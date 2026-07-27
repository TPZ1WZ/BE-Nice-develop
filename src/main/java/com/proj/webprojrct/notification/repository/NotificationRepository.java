package com.proj.webprojrct.notification.repository;

import com.proj.webprojrct.notification.entity.Notification;
import com.proj.webprojrct.notification.entity.NotificationType;
import com.proj.webprojrct.user.entity.User;
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
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Lấy tất cả thông báo của user, sắp xếp theo thời gian tạo giảm dần
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Lấy thông báo chưa đọc của user
     */
    Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Lấy thông báo theo loại
     */
    Page<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type, Pageable pageable);

    /**
     * Đếm số thông báo chưa đọc của user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    Long countUnreadByUser(@Param("user") User user);

    /**
     * Lấy danh sách thông báo chưa đọc (không phân trang)
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadByUser(@Param("user") User user);

    /**
     * Xóa thông báo cũ hơn X ngày
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    void deleteOlderThan(@Param("date") LocalDateTime date);

    /**
     * Kiểm tra có thông báo chưa đọc không
     */
    boolean existsByUserAndIsReadFalse(User user);

    /**
     * Lấy thông báo mới nhất của user theo loại
     */
    Notification findFirstByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);

    /**
     * Tìm thông báo theo data chứa order_id (JSON)
     */
    @Query(value = "SELECT * FROM notifications WHERE user_id = :userId AND data->>'order_id' = :orderId", 
           nativeQuery = true)
    List<Notification> findByUserIdAndOrderId(@Param("userId") Long userId, @Param("orderId") String orderId);

    /**
     * Tìm thông báo theo data chứa product_id (JSON)
     */
    @Query(value = "SELECT * FROM notifications WHERE user_id = :userId AND data->>'product_id' = :productId", 
           nativeQuery = true)
    List<Notification> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") String productId);

    /**
     * Lấy số thống kê thông báo theo loại
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.user = :user GROUP BY n.type")
    List<Object[]> countByTypeForUser(@Param("user") User user);
}
