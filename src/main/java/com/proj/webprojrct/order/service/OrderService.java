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
import com.proj.webprojrct.notification.service.NotificationService;
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
    private final NotificationService notificationService;

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
        
        // Tạo thông báo hủy đơn hàng
        notificationService.createOrderNotification(
            user.getId(),
            orderId,
            "CANCELED",
            "Đơn hàng #" + orderId + " đã bị hủy"
        );

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

    public String placeOrder(User user, String receiverName, String shippingAddress, String paymentMethod, String phone, String couponCode, String customerNote) {
        System.out.println("🔍 [ORDER SERVICE] placeOrder called with:");
        System.out.println("  - User: " + user.getEmail());
        System.out.println("  - Receiver Name: " + receiverName);
        System.out.println("  - Shipping Address: " + shippingAddress);
        System.out.println("  - Payment Method: " + paymentMethod);
        System.out.println("  - Phone: " + phone);
        System.out.println("  - Coupon Code: " + couponCode);
        System.out.println("  - Customer Note: " + customerNote);
        
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
        order.setReceiverName(receiverName);
        order.setPhone(phone);
        order.setQuantity(cart.getTotalQuantity());
        order.setPaymentMethod(paymentMethod);
        order.setShippingAddress(shippingAddress);
        order.setShippingFee(30000.0); // Phí ship cố định 30,000đ
        order.setCustomerNote(customerNote);
        
        System.out.println("📝 [ORDER SERVICE] Setting order fields:");
        System.out.println("  - ReceiverName set to: " + order.getReceiverName());
        System.out.println("  - ShippingAddress set to: " + order.getShippingAddress());
        System.out.println("  - PaymentMethod set to: " + order.getPaymentMethod());
        System.out.println("  - Phone set to: " + order.getPhone());
        
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
        order.setTotalDiscount(bonusDiscount);
        // Tổng cộng = (Tổng sản phẩm - Giảm giá) + Phí vận chuyển
        order.setFinalAmount(totalAfterDiscount + order.getShippingFee());

        List<OrderItem> orderItems = new ArrayList<>();
        for (var item : cart.getItems()) {
            var orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setProductPrice(item.getProductPrice());
            orderItem.setTotalPrice(item.getProductPrice() * item.getQuantity()); // Thành tiền = đơn giá * số lượng
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
        
        System.out.println("💾 [ORDER SERVICE] Saving order to database...");
        System.out.println("  - Before save - ReceiverName: " + order.getReceiverName());
        System.out.println("  - Before save - ShippingAddress: " + order.getShippingAddress());
        System.out.println("  - Before save - PaymentMethod: " + order.getPaymentMethod());
        System.out.println("  - Before save - Phone: " + order.getPhone());
        System.out.println("  - Before save - FinalAmount: " + order.getFinalAmount());
        System.out.println("  - Before save - Quantity: " + order.getQuantity());
        
        var newOrder = orderRepository.save(order);
        
        System.out.println("✅ [ORDER SERVICE] Order saved with ID: " + newOrder.getId());
        System.out.println("  - After save - ReceiverName: " + newOrder.getReceiverName());
        System.out.println("  - After save - ShippingAddress: " + newOrder.getShippingAddress());
        System.out.println("  - After save - PaymentMethod: " + newOrder.getPaymentMethod());
        System.out.println("  - After save - Phone: " + newOrder.getPhone());
        System.out.println("  - After save - FinalAmount: " + newOrder.getFinalAmount());
        System.out.println("  - After save - Quantity: " + newOrder.getQuantity());
        
        // Tạo thông báo đơn hàng mới
        notificationService.createOrderNotification(
            user.getId(),
            newOrder.getId(),
            "PENDING",
            "Đơn hàng #" + newOrder.getId() + " đã được tạo thành công. Đang chờ xác nhận."
        );
        
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
            // Chỉ đánh dấu reviewed=true nếu đã có review được approve
            // Nếu chỉ có review pending, vẫn cho phép edit/submit lại
            boolean reviewed = reviewRepository.existsByUserAndProductAndApproved(user, item.getProduct(), true);
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
            System.out.println("👤 [ORDER DTO] User info mapped:");
            System.out.println("  - ID: " + userDTO.getId());
            System.out.println("  - Full Name: " + userDTO.getFullName());
            System.out.println("  - Email: " + userDTO.getEmail());
            System.out.println("  - Phone: " + userDTO.getPhone());
        } else {
            System.out.println("⚠️ [ORDER DTO] Order has no user!");
        }

        // Fix cho đơn hàng cũ: nếu shippingFee = null hoặc 0 thì set = 30000
        Double shippingFee = order.getShippingFee();
        if (shippingFee == null || shippingFee == 0.0) {
            shippingFee = 30000.0;
        }
        
        // Tính lại finalAmount nếu cần (đơn hàng cũ có thể chưa cộng shipping fee)
        Double finalAmount = order.getFinalAmount();
        Double totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        Double totalDiscount = order.getTotalDiscount() != null ? order.getTotalDiscount() : 0.0;
        
        // Kiểm tra nếu finalAmount chưa bao gồm shipping fee
        Double expectedFinalAmount = totalAmount - totalDiscount + shippingFee;
        if (Math.abs(finalAmount - expectedFinalAmount) > 0.01) {
            // Nếu sai lệch thì tính lại
            finalAmount = expectedFinalAmount;
        }

        return OrderDTO.builder()
                .id(order.getId())
                .totalAmount(totalAmount)
                .totalDiscount(totalDiscount)
                .finalAmount(finalAmount)
                .shippingFee(shippingFee)
                .quantity(order.getQuantity())
                .receiverName(order.getReceiverName())
                .phone(order.getPhone())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .txnId(order.getTxnId())
                .customerNote(order.getCustomerNote())
                .adminNote(order.getAdminNote())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .coupon(couponDTO) // gán coupon nếu có
                .user(userDTO)
                .build();
    }
    
    /**
     * Lấy tất cả đơn hàng của user
     */
    public List<OrderDTO> getUserOrders(User user) {
        var orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return orders.stream()
                .map(order -> toOrderDTO(order, user))
                .toList();
    }
    
    /**
     * Lấy chi tiết 1 đơn hàng của user
     */
    public OrderDTO getOrderById(User user, Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        // Kiểm tra xem order có thuộc user không
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }
        
        return toOrderDTO(order, user);
    }
}
