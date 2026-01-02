package com.proj.webprojrct.admin.service;

import com.proj.webprojrct.admin.repository.AdminOrderRepository;
import com.proj.webprojrct.notification.service.NotificationService;
import com.proj.webprojrct.order.entity.Order;
import com.proj.webprojrct.product.repository.ProductRepository;
import com.proj.webprojrct.promotion.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service xử lý business logic cho quản lý đơn hàng trong admin dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminOrderService {

    private final AdminOrderRepository adminOrderRepository;
    private final NotificationService notificationService;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Lấy tất cả đơn hàng với phân trang
     */
    public Page<Order> getAllOrders(Pageable pageable) {
        log.debug("Lấy danh sách đơn hàng với phân trang: {}", pageable);
        return adminOrderRepository.findAll(pageable);
    }

    /**
     * Lấy thông tin chi tiết đơn hàng
     */
    public Optional<Order> getOrderById(Long id) {
        log.debug("Lấy thông tin đơn hàng với ID: {}", id);
        return adminOrderRepository.findById(id);
    }

    /**
     * Lấy đơn hàng theo trạng thái
     */
    public Page<Order> getOrdersByStatus(String status, Pageable pageable) {
        log.debug("Lấy đơn hàng theo trạng thái: {} với phân trang: {}", status, pageable);
        // TODO: Implement method findByStatus() trong repository
        return adminOrderRepository.findAll(pageable); // Placeholder
    }

    /**
     * Lấy đơn hàng theo người dùng
     */
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        log.debug("Lấy đơn hàng theo user ID: {} với phân trang: {}", userId, pageable);
        return adminOrderRepository.findByUser_Id(userId, pageable);
    }

    /**
     * Lấy thống kê tổng quan về đơn hàng
     */
    public Map<String, Object> getOrderStatistics() {
        log.debug("Lấy thống kê đơn hàng");
        
        long totalOrders = adminOrderRepository.count();
        
        // Tính tổng doanh thu
        Double totalRevenueDouble = adminOrderRepository.calculateTotalRevenue();
        BigDecimal totalRevenue = totalRevenueDouble != null ? 
            BigDecimal.valueOf(totalRevenueDouble) : BigDecimal.ZERO;
        
        // TODO: Implement các thống kê chi tiết khác khi có methods trong repository
        Map<String, Long> ordersByStatus = Map.of(); // Placeholder
        BigDecimal averageOrderValue = BigDecimal.ZERO; // Placeholder
        List<Object[]> topCustomers = List.of(); // Placeholder
        
        return Map.of(
            "totalOrders", totalOrders,
            "totalRevenue", totalRevenue,
            "ordersByStatus", ordersByStatus,
            "averageOrderValue", averageOrderValue,
            "topCustomers", topCustomers
        );
    }

    /**
     * Cập nhật trạng thái đơn hàng và gửi thông báo
     */
    @Transactional
    public boolean updateOrderStatus(Long orderId, String newStatus) {
        log.debug("Cập nhật trạng thái đơn hàng ID: {} thành: {}", orderId, newStatus);
        try {
            Optional<Order> orderOpt = adminOrderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                String oldStatus = order.getStatus();
                
                // Kiểm tra trạng thái hợp lệ
                if (!isValidStatus(newStatus)) {
                    throw new RuntimeException("Trạng thái không hợp lệ: " + newStatus);
                }
                
                // Nếu hủy đơn → hoàn lại số lượng và coupon
                if ("CANCELED".equalsIgnoreCase(newStatus) && !"CANCELED".equalsIgnoreCase(oldStatus)) {
                    order.getItems().forEach(orderItem -> {
                        productRepository.findById(orderItem.getProduct().getId()).ifPresent(product -> {
                            product.setStock(product.getStock() + orderItem.getQuantity());
                            productRepository.save(product);
                        });
                    });
                    
                    // Hoàn lại coupon nếu có
                    if (order.getCoupon() != null) {
                        couponRepository.findById(order.getCoupon().getId()).ifPresent(coupon -> {
                            coupon.setUsedCount(coupon.getUsedCount() - 1);
                            couponRepository.save(coupon);
                        });
                    }
                }
                
                // Cập nhật trạng thái
                order.setStatus(newStatus.toUpperCase());
                order.setUpdatedAt(LocalDateTime.now());
                adminOrderRepository.save(order);
                
                // Gửi thông báo cho user
                sendOrderStatusNotification(order, newStatus);
                
                log.info("Cập nhật trạng thái đơn hàng ID: {} thành công", orderId);
                return true;
            }
            log.warn("Không tìm thấy đơn hàng với ID: {}", orderId);
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái đơn hàng ID: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * Gửi thông báo khi trạng thái đơn hàng thay đổi
     */
    private void sendOrderStatusNotification(Order order, String newStatus) {
        String dateTime = LocalDateTime.now().format(DATE_FORMAT);
        String message;
        
        switch (newStatus.toUpperCase()) {
            case "PENDING":
                message = String.format("Đơn hàng #%d đang ở trạng thái: Chờ xác nhận.\n%s", 
                    order.getId(), dateTime);
                break;
                
            case "CONFIRMED":
                message = String.format("Đơn hàng #%d đã được xác nhận và đang được chuẩn bị.\n%s", 
                    order.getId(), dateTime);
                break;
                
            case "SHIPPING":
                message = String.format("Đơn hàng #%d đang được giao đến bạn. Vui lòng giữ liên lạc.\n%s", 
                    order.getId(), dateTime);
                break;
                
            case "COMPLETED":
                message = String.format("Đơn hàng #%d đã được giao thành công. Cảm ơn bạn đã mua sắm!\n%s", 
                    order.getId(), dateTime);
                break;
                
            case "CANCELED":
                message = String.format("Đơn hàng #%d đã bị hủy. Vui lòng kiểm tra lại hoặc liên hệ hỗ trợ.\n%s", 
                    order.getId(), dateTime);
                break;
                
            case "WAITING_FOR_PAYMENT":
                message = String.format("Đơn hàng #%d đang chờ thanh toán. Vui lòng hoàn tất để xử lý đơn.\n%s", 
                    order.getId(), dateTime);
                break;
                
            default:
                message = String.format("Đơn hàng #%d đã được cập nhật trạng thái: %s\n%s", 
                    order.getId(), newStatus, dateTime);
        }
        
        // Tạo thông báo cho user
        notificationService.createOrderNotification(
            order.getUser().getId(),
            order.getId(),
            newStatus.toUpperCase(),
            message
        );
    }
    
    /**
     * Kiểm tra trạng thái hợp lệ
     */
    private boolean isValidStatus(String status) {
        return status != null && (
            status.equalsIgnoreCase("PENDING") ||
            status.equalsIgnoreCase("CONFIRMED") ||
            status.equalsIgnoreCase("SHIPPING") ||
            status.equalsIgnoreCase("COMPLETED") ||
            status.equalsIgnoreCase("CANCELED") ||
            status.equalsIgnoreCase("WAITING_FOR_PAYMENT")
        );
    }

    /**
     * Xóa đơn hàng
     */
    @Transactional
    public boolean deleteOrder(Long orderId) {
        log.debug("Xóa đơn hàng ID: {}", orderId);
        try {
            Optional<Order> orderOpt = adminOrderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                adminOrderRepository.deleteById(orderId);
                log.info("Xóa đơn hàng ID: {} thành công", orderId);
                return true;
            }
            log.warn("Không tìm thấy đơn hàng với ID: {}", orderId);
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi xóa đơn hàng ID: {}", orderId, e);
            return false;
        }
    }

    /**
     * Lấy danh sách đơn hàng gần đây
     */
    public List<Order> getRecentOrders(int limit) {
        log.debug("Lấy {} đơn hàng gần đây", limit);
        // TODO: Implement method getRecentOrders() trong repository
        return List.of(); // Placeholder
    }

    /**
     * Lấy doanh thu theo khoảng thời gian
     */
    public Map<String, Object> getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Lấy phân tích doanh thu từ {} đến {}", startDate, endDate);
        
        // TODO: Implement methods getRevenueByDateRange() trong repository
        BigDecimal totalRevenue = BigDecimal.ZERO; // Placeholder
        List<Object[]> dailyRevenue = List.of(); // Placeholder
        
        return Map.of(
            "totalRevenue", totalRevenue,
            "dailyRevenue", dailyRevenue,
            "startDate", startDate,
            "endDate", endDate
        );
    }

    /**
     * Lấy đơn hàng cần xử lý (pending)
     */
    public List<Order> getPendingOrders() {
        log.debug("Lấy đơn hàng cần xử lý");
        // TODO: Implement method findByStatusPending() trong repository
        return List.of(); // Placeholder
    }

    /**
     * Tính toán thống kê hiệu suất
     */
    public Map<String, Object> getPerformanceMetrics() {
        log.debug("Tính toán thống kê hiệu suất đơn hàng");
        
        long totalOrders = adminOrderRepository.count();
        
        // TODO: Implement các method tính toán chi tiết
        double conversionRate = 0.0; // Placeholder
        BigDecimal averageOrderValue = BigDecimal.ZERO; // Placeholder
        
        return Map.of(
            "totalOrders", totalOrders,
            "conversionRate", conversionRate,
            "averageOrderValue", averageOrderValue
        );
    }
}