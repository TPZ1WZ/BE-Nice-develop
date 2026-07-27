package com.proj.webprojrct.notification.integration;

import com.proj.webprojrct.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * NotificationIntegrationExample - Ví dụ tích hợp thông báo vào các service
 * 
 * Hướng dẫn sử dụng:
 * 1. Inject NotificationService vào service cần tích hợp
 * 2. Gọi các method create*Notification() sau khi hoàn thành nghiệp vụ
 * 3. Notification sẽ được tạo async, không block main thread
 */
@Component
@RequiredArgsConstructor
public class NotificationIntegrationExample {

    private final NotificationService notificationService;

    /**
     * ===================================================================
     * TÍCH HỢP VÀO OrderService
     * ===================================================================
     * 
     * Thêm vào OrderService.java:
     * - Inject: private final NotificationService notificationService;
     * - Gọi sau khi tạo đơn hàng thành công
     * - Gọi sau khi cập nhật trạng thái đơn hàng
     * 
     * CÁC SỰ KIỆN TẠO THÔNG BÁO:
     */
    
    // 1. Đơn hàng được tạo mới
    public void exampleOrderCreated(Long userId, Long orderId) {
        notificationService.createOrderNotification(
            userId, 
            orderId, 
            "PENDING",
            "Đơn hàng #" + orderId + " đã được tạo và đang chờ xác nhận."
        );
    }

    // 2. Đơn hàng được xác nhận (Admin xác nhận)
    public void exampleOrderConfirmed(Long userId, Long orderId) {
        notificationService.createOrderNotification(
            userId, 
            orderId, 
            "CONFIRMED",
            "Đơn hàng #" + orderId + " đã được xác nhận và đang được chuẩn bị."
        );
    }

    // 3. Đơn hàng đang giao
    public void exampleOrderShipping(Long userId, Long orderId) {
        notificationService.createOrderNotification(
            userId, 
            orderId, 
            "SHIPPING",
            "Đơn hàng #" + orderId + " đang trên đường giao đến bạn."
        );
    }

    // 4. Đơn hàng giao thành công
    public void exampleOrderDelivered(Long userId, Long orderId) {
        notificationService.createOrderNotification(
            userId, 
            orderId, 
            "DELIVERED",
            "Đơn hàng #" + orderId + " đã được giao thành công. Cảm ơn bạn đã mua hàng!"
        );
    }

    // 5. Đơn hàng bị hủy
    public void exampleOrderCancelled(Long userId, Long orderId, String reason) {
        notificationService.createOrderNotification(
            userId, 
            orderId, 
            "CANCELLED",
            "Đơn hàng #" + orderId + " đã bị hủy. " + (reason != null ? "Lý do: " + reason : "")
        );
    }

    /**
     * ===================================================================
     * CODE MẪU TÍCH HỢP VÀO OrderService.placeOrder():
     * ===================================================================
     * 
     * // Sau khi save order thành công
     * orderRepository.save(order);
     * 
     * // Tạo thông báo đơn hàng mới
     * notificationService.createOrderNotification(
     *     user.getId(),
     *     order.getId(),
     *     "PENDING",
     *     "Đơn hàng #" + order.getId() + " đã được tạo và đang chờ xác nhận."
     * );
     * 
     * ===================================================================
     * CODE MẪU TÍCH HỢP VÀO OrderService.updateOrderStatus():
     * ===================================================================
     * 
     * order.setStatus(newStatus);
     * orderRepository.save(order);
     * 
     * // Tạo thông báo cập nhật trạng thái
     * String message = switch (newStatus) {
     *     case "CONFIRMED" -> "Đơn hàng đã được xác nhận và đang được chuẩn bị.";
     *     case "SHIPPING" -> "Đơn hàng đang trên đường giao đến bạn.";
     *     case "DELIVERED" -> "Đơn hàng đã được giao thành công!";
     *     default -> "Trạng thái đơn hàng đã được cập nhật.";
     * };
     * 
     * notificationService.createOrderNotification(
     *     order.getUser().getId(),
     *     order.getId(),
     *     newStatus,
     *     message
     * );
     */

    /**
     * ===================================================================
     * TÍCH HỢP VÀO FavoriteService
     * ===================================================================
     * 
     * Thêm vào FavoriteService.java:
     * - Inject: private final NotificationService notificationService;
     * 
     * CÁC SỰ KIỆN TẠO THÔNG BÁO:
     */
    
    // 1. Sản phẩm yêu thích giảm giá
    public void exampleFavoritePriceDrop(Long userId, Long productId, String productName, double discount) {
        notificationService.createFavoriteNotification(
            userId,
            productId,
            productName,
            "PRICE_DROP",
            productName + " trong danh sách yêu thích đang giảm giá " + (int)discount + "%!"
        );
    }

    // 2. Sản phẩm yêu thích có hàng trở lại
    public void exampleFavoriteBackInStock(Long userId, Long productId, String productName) {
        notificationService.createFavoriteNotification(
            userId,
            productId,
            productName,
            "BACK_IN_STOCK",
            productName + " đã có hàng trở lại. Đặt hàng ngay!"
        );
    }

    // 3. Sản phẩm yêu thích sắp hết hàng
    public void exampleFavoriteLowStock(Long userId, Long productId, String productName, int remaining) {
        notificationService.createFavoriteNotification(
            userId,
            productId,
            productName,
            "LOW_STOCK",
            productName + " chỉ còn " + remaining + " sản phẩm. Mua ngay kẻo hết!"
        );
    }

    /**
     * ===================================================================
     * CODE MẪU: Tự động thông báo khi sản phẩm yêu thích giảm giá
     * ===================================================================
     * 
     * Tạo một scheduled job hoặc event listener:
     * 
     * @Scheduled(cron = "0 0 8 * * *") // Chạy mỗi ngày 8h sáng
     * public void checkFavoritePriceDrops() {
     *     List<User> allUsers = userRepository.findAll();
     *     
     *     for (User user : allUsers) {
     *         List<Favorite> favorites = favoriteRepository.findByUser(user);
     *         
     *         for (Favorite fav : favorites) {
     *             Product product = fav.getProduct();
     *             
     *             // Kiểm tra sản phẩm có giảm giá không
     *             if (product.getDiscountPercent() > 0) {
     *                 notificationService.createFavoriteNotification(
     *                     user.getId(),
     *                     product.getId(),
     *                     product.getName(),
     *                     "PRICE_DROP",
     *                     product.getName() + " đang giảm " + product.getDiscountPercent() + "%!"
     *                 );
     *             }
     *         }
     *     }
     * }
     */

    /**
     * ===================================================================
     * TÍCH HỢP VÀO CouponService / PromotionService
     * ===================================================================
     * 
     * CÁC SỰ KIỆN TẠO THÔNG BÁO:
     */
    
    // 1. User nhận mã giảm giá mới
    public void exampleCouponReceived(Long userId, String couponCode, int discountValue) {
        notificationService.createCouponNotification(
            userId,
            couponCode,
            "Bạn nhận được mã " + couponCode + " giảm " + discountValue + "đ. Sử dụng ngay!"
        );
    }

    // 2. Mã giảm giá sắp hết hạn
    public void exampleCouponExpiring(Long userId, String couponCode, String expiryDate) {
        notificationService.createCouponNotification(
            userId,
            couponCode,
            "Mã " + couponCode + " sẽ hết hạn vào " + expiryDate + ". Dùng ngay kẻo lỡ!"
        );
    }

    /**
     * ===================================================================
     * CODE MẪU: Tự động thông báo mã giảm giá sắp hết hạn
     * ===================================================================
     * 
     * @Scheduled(cron = "0 0 9 * * *") // Chạy mỗi ngày 9h sáng
     * public void checkExpiringCoupons() {
     *     LocalDateTime threeDaysLater = LocalDateTime.now().plusDays(3);
     *     
     *     List<Coupon> expiringCoupons = couponRepository
     *         .findByEndDateBetween(LocalDateTime.now(), threeDaysLater);
     *     
     *     for (Coupon coupon : expiringCoupons) {
     *         // Gửi cho tất cả user đã lưu coupon này
     *         List<User> users = userCouponRepository.findUsersByCouponId(coupon.getId());
     *         
     *         for (User user : users) {
     *             notificationService.createCouponNotification(
     *                 user.getId(),
     *                 coupon.getCode(),
     *                 "Mã " + coupon.getCode() + " sắp hết hạn. Sử dụng ngay!"
     *             );
     *         }
     *     }
     * }
     */

    /**
     * ===================================================================
     * TÍCH HỢP VÀO ProductService
     * ===================================================================
     * 
     * CÁC SỰ KIỆN TẠO THÔNG BÁO:
     */
    
    // 1. Sản phẩm mới ra mắt - gửi cho tất cả user
    public void exampleNewProductLaunch(Long productId, String productName) {
        notificationService.broadcastNotification(
            com.proj.webprojrct.notification.entity.NotificationType.PRODUCT,
            "Sản phẩm mới",
            productName + " vừa ra mắt. Xem ngay!",
            java.util.Map.of("product_id", productId, "product_name", productName)
        );
    }

    /**
     * ===================================================================
     * THÔNG BÁO HỆ THỐNG
     * ===================================================================
     */
    
    // 1. Chào mừng user mới đăng ký
    public void exampleWelcomeNewUser(Long userId, String username) {
        notificationService.createSystemNotification(
            userId,
            "Chào mừng đến Nike Store",
            "Xin chào " + username + "! Cảm ơn bạn đã đăng ký. Khám phá hàng nghìn sản phẩm chính hãng!"
        );
    }

    // 2. Thông báo bảo trì hệ thống
    public void exampleSystemMaintenance() {
        notificationService.broadcastNotification(
            com.proj.webprojrct.notification.entity.NotificationType.SYSTEM,
            "Thông báo bảo trì",
            "Hệ thống sẽ bảo trì từ 00:00 - 02:00 ngày mai. Vui lòng hoàn tất đơn hàng trước thời gian này.",
            new java.util.HashMap<>()
        );
    }
}
