package com.proj.webprojrct.notification.controller;

import com.proj.webprojrct.common.util.SecurityUtil;
import com.proj.webprojrct.notification.dto.NotificationRequest;
import com.proj.webprojrct.notification.dto.NotificationResponse;
import com.proj.webprojrct.notification.entity.NotificationType;
import com.proj.webprojrct.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NotificationController - REST API endpoints cho thông báo
 * 
 * Endpoints:
 * - GET /api/notifications - Lấy danh sách thông báo
 * - GET /api/notifications/unread - Lấy thông báo chưa đọc
 * - GET /api/notifications/count-unread - Đếm thông báo chưa đọc
 * - PUT /api/notifications/{id}/read - Đánh dấu đã đọc
 * - PUT /api/notifications/read-all - Đánh dấu tất cả đã đọc
 * - DELETE /api/notifications/{id} - Xóa thông báo
 * - DELETE /api/notifications - Xóa tất cả
 * - GET /api/notifications/statistics - Thống kê thông báo
 * - POST /api/admin/notifications/broadcast - Gửi thông báo cho tất cả (Admin)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "API quản lý thông báo")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Lấy danh sách thông báo của user hiện tại
     * GET /api/notifications?page=0&size=20
     */
    @GetMapping("/notifications")
    @Operation(summary = "Lấy danh sách thông báo", 
               description = "Lấy tất cả thông báo của user với phân trang")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        Page<NotificationResponse> notifications = notificationService
                .getUserNotifications(userId, page, size);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Lấy thông báo chưa đọc
     * GET /api/notifications/unread?page=0&size=20
     */
    @GetMapping("/notifications/unread")
    @Operation(summary = "Lấy thông báo chưa đọc", 
               description = "Lấy các thông báo chưa đọc của user")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        Page<NotificationResponse> notifications = notificationService
                .getUnreadNotifications(userId, page, size);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Đếm số thông báo chưa đọc (hiển thị badge)
     * GET /api/notifications/count-unread
     * Response: {"count": 5}
     */
    @GetMapping("/notifications/count-unread")
    @Operation(summary = "Đếm thông báo chưa đọc", 
               description = "Trả về số lượng thông báo chưa đọc để hiển thị badge")
    public ResponseEntity<Map<String, Long>> countUnreadNotifications() {
        Long userId = SecurityUtil.getCurrentUserId();
        Long count = notificationService.countUnreadNotifications(userId);
        
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Đánh dấu thông báo đã đọc
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/notifications/{id}/read")
    @Operation(summary = "Đánh dấu đã đọc", 
               description = "Đánh dấu một thông báo cụ thể là đã đọc")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu thông báo là đã đọc"));
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     * PUT /api/notifications/read-all
     */
    @PutMapping("/notifications/read-all")
    @Operation(summary = "Đánh dấu tất cả đã đọc", 
               description = "Đánh dấu tất cả thông báo của user là đã đọc")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu tất cả thông báo là đã đọc"));
    }

    /**
     * Xóa thông báo
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/notifications/{id}")
    @Operation(summary = "Xóa thông báo", 
               description = "Xóa một thông báo cụ thể")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        
        return ResponseEntity.ok(Map.of("message", "Đã xóa thông báo"));
    }

    /**
     * Xóa tất cả thông báo
     * DELETE /api/notifications
     */
    @DeleteMapping("/notifications")
    @Operation(summary = "Xóa tất cả thông báo", 
               description = "Xóa tất cả thông báo của user")
    public ResponseEntity<Map<String, String>> deleteAllNotifications() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.deleteAllNotifications(userId);
        
        return ResponseEntity.ok(Map.of("message", "Đã xóa tất cả thông báo"));
    }

    /**
     * Lấy thống kê thông báo theo loại
     * GET /api/notifications/statistics
     * Response: {"ORDER": 10, "FAVORITE": 5, "COUPON": 3}
     */
    @GetMapping("/notifications/statistics")
    @Operation(summary = "Thống kê thông báo", 
               description = "Lấy số lượng thông báo theo từng loại")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        Map<String, Long> statistics = notificationService.getNotificationStatistics(userId);
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * [ADMIN] Gửi thông báo broadcast cho tất cả user
     * POST /api/admin/notifications/broadcast
     * Body: {"type": "SYSTEM", "title": "...", "message": "...", "data": {...}}
     */
    @PostMapping("/admin/notifications/broadcast")
    @Operation(summary = "Broadcast thông báo (Admin)", 
               description = "Gửi thông báo cho tất cả người dùng trong hệ thống")
    public ResponseEntity<Map<String, String>> broadcastNotification(
            @RequestBody NotificationRequest request) {
        
        NotificationType type = NotificationType.valueOf(request.getType());
        notificationService.broadcastNotification(
                type, 
                request.getTitle(), 
                request.getMessage(), 
                request.getData()
        );
        
        return ResponseEntity.ok(Map.of("message", "Đã gửi thông báo cho tất cả người dùng"));
    }

    /**
     * [ADMIN] Tạo thông báo cho user cụ thể
     * POST /api/admin/notifications/user/{userId}
     */
    @PostMapping("/admin/notifications/user/{userId}")
    @Operation(summary = "Tạo thông báo cho user (Admin)", 
               description = "Tạo thông báo cho một user cụ thể")
    public ResponseEntity<Map<String, String>> createNotificationForUser(
            @PathVariable Long userId,
            @RequestBody NotificationRequest request) {
        
        NotificationType type = NotificationType.valueOf(request.getType());
        notificationService.createNotification(
                userId, 
                type, 
                request.getTitle(), 
                request.getMessage(), 
                request.getData()
        );
        
        return ResponseEntity.ok(Map.of("message", "Đã tạo thông báo cho user " + userId));
    }
}
