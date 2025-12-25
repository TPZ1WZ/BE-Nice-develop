package com.proj.webprojrct.notification.service;

import com.proj.webprojrct.notification.dto.NotificationResponse;
import com.proj.webprojrct.notification.entity.Notification;
import com.proj.webprojrct.notification.entity.NotificationType;
import com.proj.webprojrct.notification.mapper.NotificationMapper;
import com.proj.webprojrct.notification.repository.NotificationRepository;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NotificationService - Service xử lý nghiệp vụ thông báo
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Tạo thông báo mới (async để không block main thread)
     */
    @Async
    public void createNotification(Long userId, NotificationType type, String title, 
                                   String message, Map<String, Object> data) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .message(message)
                    .data(data)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Created notification for user {}: {} - {}", userId, type, title);
        } catch (Exception e) {
            log.error("Error creating notification for user {}", userId, e);
        }
    }

    /**
     * Tạo thông báo đơn hàng
     */
    @Async
    public void createOrderNotification(Long userId, Long orderId, String status, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("order_id", orderId);
        data.put("status", status);
        
        String title = getOrderNotificationTitle(status);
        createNotification(userId, NotificationType.ORDER, title, message, data);
    }

    /**
     * Tạo thông báo sản phẩm yêu thích
     */
    @Async
    public void createFavoriteNotification(Long userId, Long productId, String productName, 
                                          String eventType, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("product_id", productId);
        data.put("product_name", productName);
        data.put("event_type", eventType);
        
        String title = getFavoriteNotificationTitle(eventType);
        createNotification(userId, NotificationType.FAVORITE, title, message, data);
    }

    /**
     * Tạo thông báo khuyến mãi
     */
    @Async
    public void createCouponNotification(Long userId, String couponCode, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("coupon_code", couponCode);
        
        String title = "Mã giảm giá mới";
        createNotification(userId, NotificationType.COUPON, title, message, data);
    }

    /**
     * Tạo thông báo sản phẩm mới
     */
    @Async
    public void createProductNotification(Long userId, Long productId, String productName, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("product_id", productId);
        data.put("product_name", productName);
        
        String title = "Sản phẩm mới";
        createNotification(userId, NotificationType.PRODUCT, title, message, data);
    }

    /**
     * Tạo thông báo hệ thống
     */
    @Async
    public void createSystemNotification(Long userId, String title, String message) {
        createNotification(userId, NotificationType.SYSTEM, title, message, new HashMap<>());
    }

    /**
     * Gửi thông báo cho tất cả user (broadcast)
     */
    @Async
    public void broadcastNotification(NotificationType type, String title, String message, 
                                      Map<String, Object> data) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            createNotification(user.getId(), type, title, message, data);
        }
        log.info("Broadcast notification to {} users", allUsers.size());
    }

    /**
     * Lấy danh sách thông báo của user (có phân trang)
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository
                .findByUserOrderByCreatedAtDesc(user, pageable);

        return notifications.map(notificationMapper::toResponse);
    }

    /**
     * Lấy thông báo chưa đọc
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user, pageable);

        return notifications.map(notificationMapper::toResponse);
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countUnreadByUser(user);
    }

    /**
     * Đánh dấu thông báo đã đọc
     */
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationRepository.markAllAsReadByUser(user);
        log.info("Marked all notifications as read for user {}", userId);
    }

    /**
     * Xóa thông báo
     */
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        notificationRepository.delete(notification);
        log.info("Deleted notification {} for user {}", notificationId, userId);
    }

    /**
     * Xóa tất cả thông báo của user
     */
    public void deleteAllNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        
        notificationRepository.deleteAll(notifications);
        log.info("Deleted all notifications for user {}", userId);
    }

    /**
     * Xóa thông báo cũ (chạy định kỳ, xóa thông báo > 30 ngày)
     */
    public void cleanOldNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOlderThan(thirtyDaysAgo);
        log.info("Cleaned notifications older than {}", thirtyDaysAgo);
    }

    /**
     * Lấy thống kê thông báo theo loại
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getNotificationStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Object[]> stats = notificationRepository.countByTypeForUser(user);
        
        return stats.stream()
                .collect(Collectors.toMap(
                    arr -> ((NotificationType) arr[0]).name(),
                    arr -> (Long) arr[1]
                ));
    }

    // Helper methods để tạo title động
    
    private String getOrderNotificationTitle(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Đơn hàng đang chờ xử lý";
            case "CONFIRMED" -> "Đơn hàng đã xác nhận";
            case "SHIPPING" -> "Đơn hàng đang giao";
            case "DELIVERED" -> "Giao hàng thành công";
            case "CANCELLED" -> "Đơn hàng đã hủy";
            case "RETURNED" -> "Đơn hàng hoàn trả";
            default -> "Cập nhật đơn hàng";
        };
    }

    private String getFavoriteNotificationTitle(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "PRICE_DROP" -> "Giảm giá - Sản phẩm yêu thích";
            case "BACK_IN_STOCK" -> "Hàng về - Sản phẩm yêu thích";
            case "LOW_STOCK" -> "Sắp hết hàng - Sản phẩm yêu thích";
            default -> "Cập nhật sản phẩm yêu thích";
        };
    }
}
