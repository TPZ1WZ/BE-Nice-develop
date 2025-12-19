package com.proj.webprojrct.admin.controller;

import com.proj.webprojrct.order.repository.OrderRepository;
import com.proj.webprojrct.product.repository.ProductRepository;
import com.proj.webprojrct.promotion.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller quản lý đơn hàng cho Android admin app
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderRestController {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    /**
     * Lấy danh sách đơn hàng với filter theo status
     * GET /api/v1/admin/orders?status=pending
     */
    @GetMapping
    public List<Map<String, Object>> getAllOrders(@RequestParam(required = false) String status) {
        var orders = (status == null || status.trim().isEmpty()) 
            ? orderRepository.findAll() 
            : orderRepository.findByStatus(status.toUpperCase());
            
        return orders.stream().map(order -> {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", order.getId());
            dto.put("userName", order.getUser() != null ? order.getUser().getFullName() : "Khách lẻ");
            dto.put("email", order.getUser() != null ? order.getUser().getEmail() : "");
            dto.put("phone", order.getPhone() != null ? order.getPhone() : "");
            dto.put("shippingAddress", order.getShippingAddress() != null ? order.getShippingAddress() : "");
            dto.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
            dto.put("status", order.getStatus() != null ? order.getStatus() : "PENDING");
            
            // Calculate totalAmount if null (for old data)
            double totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
            double finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : 0.0;
            
            // If both are 0, calculate from items
            if (totalAmount == 0.0 && order.getItems() != null && !order.getItems().isEmpty()) {
                totalAmount = order.getItems().stream()
                    .mapToDouble(item -> item.getProductPrice() * item.getQuantity())
                    .sum();
                finalAmount = totalAmount - (order.getTotalDiscount() != null ? order.getTotalDiscount() : 0.0);
            }
            
            dto.put("totalAmount", totalAmount);
            dto.put("totalDiscount", order.getTotalDiscount() != null ? order.getTotalDiscount() : 0.0);
            dto.put("finalAmount", finalAmount);
            dto.put("quantity", order.getItems() != null ? order.getItems().stream().mapToInt(i -> i.getQuantity()).sum() : 0);
            dto.put("createdAt", order.getCreatedAt());
            return dto;
        }).toList();
    }

    /**
     * Lấy chi tiết đơn hàng
     * GET /api/v1/admin/orders/{id}
     */
    @GetMapping("/{id}")
    public Object getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    Map<String, Object> dto = new LinkedHashMap<>();
                    dto.put("id", order.getId());
                    dto.put("userName", order.getUser() != null ? order.getUser().getFullName() : "Khách lẻ");
                    dto.put("email", order.getUser() != null ? order.getUser().getEmail() : "");
                    dto.put("phone", order.getPhone());
                    dto.put("shippingAddress", order.getShippingAddress());
                    dto.put("paymentMethod", order.getPaymentMethod());
                    dto.put("status", order.getStatus());
                    dto.put("totalAmount", order.getTotalAmount());
                    dto.put("totalDiscount", order.getTotalDiscount() != null ? order.getTotalDiscount() : 0.0);
                    dto.put("finalAmount", order.getFinalAmount());
                    dto.put("quantity", order.getItems() != null ? order.getItems().stream().mapToInt(i -> i.getQuantity()).sum() : 0);
                    dto.put("createdAt", order.getCreatedAt());
                    dto.put("coupon", order.getCoupon() != null ? order.getCoupon().getCode() : null);

                    List<Map<String, Object>> items = order.getItems().stream().map(i -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", i.getId());
                        m.put("productName", i.getProduct().getName());
                        m.put("productPrice", i.getProductPrice());
                        m.put("quantity", i.getQuantity());
                        m.put("size", i.getSize());
                        m.put("totalPrice", i.getProductPrice() * i.getQuantity());
                        m.put("imageUrl", i.getProduct().getImages() != null && !i.getProduct().getImages().isEmpty() 
                            ? i.getProduct().getImages().get(0) : "");
                        return m;
                    }).toList();

                    dto.put("items", items);
                    return dto;
                })
                .orElse(Map.of("error", "Đơn hàng không tồn tại"));
    }

    /**
     * Cập nhật trạng thái đơn hàng - PHẢI THEO THỨ TỰ
     * PATCH /api/v1/admin/orders/{id}/status?status=confirmed
     * 
     * Flow: PENDING → CONFIRMED → SHIPPING → COMPLETED
     * Có thể HỦY ở bất kỳ bước nào (trừ COMPLETED)
     */
    @PatchMapping("/{id}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        var order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        
        String currentStatus = order.getStatus().toUpperCase();
        String newStatus = status.toUpperCase();
        
        // Kiểm tra trạng thái hợp lệ
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            return Map.of(
                "success", false, 
                "message", "Không thể chuyển từ " + currentStatus + " sang " + newStatus + ". Phải theo thứ tự: PENDING → CONFIRMED → SHIPPING → COMPLETED"
            );
        }
        
        // Xử lý chuyển trạng thái
        switch (newStatus) {
            case "CONFIRMED":
                if (!"PENDING".equals(currentStatus)) {
                    return Map.of("success", false, "message", "Chỉ có thể xác nhận đơn hàng PENDING");
                }
                order.setStatus("CONFIRMED");
                break;
                
            case "SHIPPING":
                if (!"CONFIRMED".equals(currentStatus)) {
                    return Map.of("success", false, "message", "Chỉ có thể chuyển sang SHIPPING từ CONFIRMED");
                }
                order.setStatus("SHIPPING");
                break;
                
            case "COMPLETED":
                if (!"SHIPPING".equals(currentStatus)) {
                    return Map.of("success", false, "message", "Chỉ có thể hoàn tất đơn hàng đang SHIPPING");
                }
                order.setStatus("COMPLETED");
                break;
                
            case "CANCELED":
                if ("COMPLETED".equals(currentStatus)) {
                    return Map.of("success", false, "message", "Không thể hủy đơn hàng đã hoàn tất");
                }
                order.setStatus("CANCELED");
                
                // Hoàn lại stock khi hủy đơn
                order.getItems().forEach(oi -> {
                    var p = productRepository.findById(oi.getProduct().getId()).orElseThrow();
                    p.setStock(p.getStock() + oi.getQuantity());
                    productRepository.save(p);
                });
                
                // Hoàn lại coupon
                Optional.ofNullable(order.getCoupon())
                    .flatMap(c -> couponRepository.findById(c.getId()))
                    .ifPresent(coupon -> {
                        coupon.setUsedCount(coupon.getUsedCount() - 1);
                        couponRepository.save(coupon);
                    });
                break;
                
            default:
                return Map.of("success", false, "message", "Trạng thái không hợp lệ");
        }
        
        orderRepository.save(order);
        return Map.of(
            "success", true, 
            "message", "Cập nhật trạng thái thành công", 
            "status", order.getStatus(),
            "statusMessage", getStatusMessage(order.getStatus())
        );
    }
    
    /**
     * Kiểm tra xem có thể chuyển từ currentStatus sang newStatus không
     */
    private boolean isValidStatusTransition(String current, String target) {
        // Có thể hủy ở bất kỳ trạng thái nào (trừ COMPLETED)
        if ("CANCELED".equals(target)) {
            return !"COMPLETED".equals(current);
        }
        
        // Kiểm tra flow bình thường
        return switch (current) {
            case "PENDING" -> "CONFIRMED".equals(target) || "CANCELED".equals(target);
            case "CONFIRMED" -> "SHIPPING".equals(target) || "CANCELED".equals(target);
            case "SHIPPING" -> "COMPLETED".equals(target) || "CANCELED".equals(target);
            case "COMPLETED" -> false; // Không thể chuyển từ COMPLETED
            case "CANCELED" -> false; // Không thể chuyển từ CANCELED
            default -> false;
        };
    }
    
    /**
     * Lấy message theo trạng thái
     */
    private String getStatusMessage(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPING" -> "Đang giao";
            case "COMPLETED" -> "Hoàn tất";
            case "CANCELED" -> "Đã hủy";
            default -> status;
        };
    }
}
