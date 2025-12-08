package com.proj.webprojrct.order.service;

import com.proj.webprojrct.cart.repository.CartRepository;
import com.proj.webprojrct.order.dto.*;
import com.proj.webprojrct.order.entity.Order;
import com.proj.webprojrct.order.entity.OrderItem;
import com.proj.webprojrct.order.repository.OrderItemRepository;
import com.proj.webprojrct.order.repository.OrderRepository;
import com.proj.webprojrct.payment.PaymentService;
import com.proj.webprojrct.payment.VnpayDTO;
import com.proj.webprojrct.product.repository.ProductRepository;
import com.proj.webprojrct.promotion.entity.Coupon;
import com.proj.webprojrct.promotion.repository.CouponRepository;
import com.proj.webprojrct.review.repository.ReviewRepository;
import com.proj.webprojrct.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;
    private final ReviewRepository reviewRepository;

    public void cancelOrder(User user, Long orderId) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này.");
        }
        if (!List.of("PENDING", "WAITING_FOR_PAYMENT", "PAID").contains(order.getStatus().toUpperCase())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng ở trạng thái chờ xử lý hoặc chờ thanh toán.");
        }
        order.setStatus("CANCELED");
        orderRepository.save(order);

        for (var item : order.getItems()) {
            productRepository.findById(item.getProduct().getId()).ifPresent(pt -> {
                pt.setStock(pt.getStock() + item.getQuantity());
                productRepository.save(pt);
            });
        }
        Optional.ofNullable(order.getCoupon()).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() - 1);
            couponRepository.save(coupon);
        });
    }

    public String placeOrder(User user, String shippingAddress, String paymentMethod, String phone, String couponCode) {
        var cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng cho người dùng."));
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng.");
        }
        Coupon couponItem = null;
        double totalBeforeDiscount = cart.getTotalPrice();

        if (StringUtils.hasText(couponCode)) {
            couponItem = couponRepository.findByCode(couponCode).orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá."));
            boolean expired = couponItem.getEndDate() != null && couponItem.getEndDate().isBefore(LocalDateTime.now());
            boolean notStarted = couponItem.getStartDate() != null && couponItem.getStartDate().isAfter(LocalDateTime.now());
            boolean exceededUsage = couponItem.getUsageLimit() != null && couponItem.getUsedCount() >= couponItem.getUsageLimit();
            boolean belowMin = totalBeforeDiscount < couponItem.getMinOrderAmount();
            boolean inactive = !Boolean.TRUE.equals(couponItem.getIsActive());
            if (expired || notStarted || exceededUsage || belowMin || inactive) {
                throw new RuntimeException("Mã giảm giá không hợp lệ hoặc đã hết hạn.");
            }
        }

        var order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setPhone(phone);
        order.setQuantity(cart.getTotalQuantity());
        order.setPaymentMethod(paymentMethod);
        order.setShippingAddress(shippingAddress);
        order.setCoupon(couponItem);
        double totalAfterDiscount = totalBeforeDiscount;
        double bonusDiscount = 0.0;
        if (couponItem != null) {
            order.setCoupon(couponItem);
            bonusDiscount = couponItem.calculateDiscount(totalBeforeDiscount);
            totalAfterDiscount = totalBeforeDiscount - bonusDiscount;
            couponItem.setUsedCount(couponItem.getUsedCount() + 1);
            couponRepository.save(couponItem);
        }
        order.setTotalAmount(totalBeforeDiscount);
        order.setFinalAmount(totalAfterDiscount);
        order.setTotalDiscount(bonusDiscount);

        List<OrderItem> orderItems = new ArrayList<>();
        for (var item : cart.getItems()) {
            var orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setProductPrice(item.getProductPrice());
            orderItem.setSize(item.getSize());
            orderItems.add(orderItem);
        }

        cart.getItems().forEach(ct -> {
            productRepository.findById(ct.getProduct().getId()).ifPresent(pt -> {
                if (pt.getStock() < ct.getQuantity()) {
                    throw new RuntimeException("Sản phẩm " + pt.getName() + "(%d)".formatted(pt.getId()) + " không đủ số lượng tồn kho");
                }
                pt.setStock(pt.getStock() - ct.getQuantity());
                productRepository.save(pt);
            });
        });
        order.setItems(orderItems);
        var newOrder = orderRepository.save(order);
        cartRepository.delete(cart);

        if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            VnpayDTO vnPayOrder = paymentService.createPayment(new PaymentService.VnPayBody(newOrder.getTotalAmount(), "Thanh toan"));
            newOrder.setTxnId(vnPayOrder.getTxnId());
            newOrder.setStatus("WAITING_FOR_PAYMENT");
            orderRepository.save(newOrder);
            return vnPayOrder.getPaymentUrl();
        }
        return null;
    }

    public OrderDTO toOrderDTO(Order order, User user) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item -> {
            boolean reviewed = reviewRepository.existsByUserAndProduct(user, item.getProduct());
            var p = item.getProduct();
            var productDTO = ProductOrderDTO.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .slug(p.getSlug())
                    .subTitle(p.getSubTitle())
                    .description(p.getDescription())
                    .price(p.getPrice())
                    .stock(p.getStock())
                    .isDelete(p.isDelete())
                    .images(p.getImages())
                    .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                    .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                    .build();
            return OrderItemDTO.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .productImages(item.getProduct().getImages())
                    .quantity(item.getQuantity())
                    .productPrice(item.getProductPrice())
                    .totalPrice(item.getTotalPrice())
                    .size(item.getSize())
                    .reviewed(reviewed)
                    .product(productDTO)
                    .build();
        }).toList();

        CouponOrderDTO couponDTO = null;
        if (order.getCoupon() != null) {
            var c = order.getCoupon();
            couponDTO = CouponOrderDTO.builder()
                    .id(c.getId())
                    .code(c.getCode())
                    .name(c.getName())
                    .description(c.getDescription())
                    .discountType(c.getDiscountType())
                    .discountValue(c.getDiscountValue())
                    .minOrderAmount(c.getMinOrderAmount())
                    .maxDiscountAmount(c.getMaxDiscountAmount())
                    .usageLimit(c.getUsageLimit())
                    .usedCount(c.getUsedCount())
                    .startDate(c.getStartDate())
                    .endDate(c.getEndDate())
                    .isActive(c.getIsActive())
                    .validNow(c.isValid()) // gọi trực tiếp hàm helper trong entity
                    .remainingUses((double) (c.getUsageLimit() - c.getUsedCount()))
                    .build();
        }
        UserOrderDTO userDTO = null;
        if (order.getUser() != null) {
            var u = order.getUser();
            userDTO = UserOrderDTO.builder()
                    .id(u.getId())
                    .fullName(u.getFullName())   // hoặc getName() tùy entity
                    .email(u.getEmail())
                    .phone(u.getPhone())
                    .build();
        }

        return OrderDTO.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .totalDiscount(order.getTotalDiscount())
                .finalAmount(order.getFinalAmount())
                .quantity(order.getQuantity())
                .phone(order.getPhone())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .txnId(order.getTxnId())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .coupon(couponDTO) // gán coupon nếu có
                .user(userDTO)
                .build();
    }
}
