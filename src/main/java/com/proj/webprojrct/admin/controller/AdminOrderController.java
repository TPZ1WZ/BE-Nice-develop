package com.proj.webprojrct.admin.controller;

import com.proj.webprojrct.order.repository.OrderRepository;
import com.proj.webprojrct.product.repository.ProductRepository;
import com.proj.webprojrct.promotion.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller quản lý đơn hàng trong admin dashboard
 */
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    @GetMapping
    public String orderPage(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        model.addAttribute("total", orderRepository.countAllOrders());
        model.addAttribute("pending", orderRepository.countByStatus("pending"));
        model.addAttribute("confirmed", orderRepository.countByStatus("confirmed"));
        model.addAttribute("shipping", orderRepository.countByStatus("shipping"));
        model.addAttribute("completed", orderRepository.countByStatus("completed"));
        model.addAttribute("canceled", orderRepository.countByStatus("canceled"));
        model.addAttribute("waitPaid", orderRepository.countByStatus("WAITING_FOR_PAYMENT"));
        return "admin/orders";
    }

    @GetMapping("update-status")
    @ResponseBody
    public Object updateOrderStatus(@RequestParam Long id, @RequestParam String status) {
        var order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        switch (status) {
            case "confirmed":
                order.setStatus("confirmed");
                break;
            case "shipping":
                order.setStatus("shipping");
                break;
            case "completed":
                order.setStatus("completed");
                break;
            case "canceled":
                order.setStatus("canceled");
                order.getItems().forEach(oi -> {
                    var p = productRepository.findById(oi.getProduct().getId()).orElseThrow();
                    p.setStock(p.getStock() + oi.getQuantity());
                    productRepository.save(p);
                });
                Optional.ofNullable(order.getCoupon()).flatMap(c -> couponRepository.findById(c.getId())).ifPresent(coupon -> {
                    coupon.setUsedCount(coupon.getUsedCount() - 1);
                    couponRepository.save(coupon);
                });

                break;
        }
        orderRepository.save(order);
        return Map.of("success", true);
    }

    @GetMapping("{id}")
    @ResponseBody
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
                    dto.put("createdAt", order.getCreatedAt());
                    dto.put("coupon", order.getCoupon() != null ? order.getCoupon().getCode() : null);

                    List<Map<String, Object>> items = order.getItems().stream().map(i -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("productName", i.getProduct().getName());
                        m.put("quantity", i.getQuantity());
                        m.put("price", i.getProductPrice());
                        m.put("size", i.getSize());
                        m.put("subtotal", i.getProductPrice() * i.getQuantity());
                        return m;
                    }).toList();

                    dto.put("items", items);
                    return dto;
                })
                .orElse(Map.of());
    }
}