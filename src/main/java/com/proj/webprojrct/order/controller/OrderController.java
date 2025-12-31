package com.proj.webprojrct.order.controller;

import com.proj.webprojrct.order.dto.OrderDTO;
import com.proj.webprojrct.order.dto.PlaceOrderRequest;
import com.proj.webprojrct.order.service.OrderService;
import com.proj.webprojrct.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing user orders")
public class OrderController {
    
    private final OrderService orderService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Place a new order", description = "Create order from cart items")
    @ApiResponse(responseCode = "200", description = "Order placed successfully")
    @PostMapping
    public ResponseEntity<Object> placeOrder(
            @AuthenticationPrincipal User user,
            @RequestBody PlaceOrderRequest request) {
        try {
            System.out.println("📦 [ORDER] Received order request:");
            System.out.println("  - Receiver Name: " + request.getReceiverName());
            System.out.println("  - Shipping Address: " + request.getShippingAddress());
            System.out.println("  - Payment Method: " + request.getPaymentMethod());
            System.out.println("  - Phone: " + request.getPhone());
            System.out.println("  - Coupon Code: " + request.getCouponCode());
            System.out.println("  - Customer Note: " + request.getCustomerNote());
            
            String result = orderService.placeOrder(
                user, 
                request.getReceiverName(),
                request.getShippingAddress(), 
                request.getPaymentMethod(), 
                request.getPhone(), 
                request.getCouponCode(),
                request.getCustomerNote()
            );
            
            if (result != null && result.startsWith("http")) {
                // VNPay payment URL
                return ResponseEntity.ok(Map.of(
                    "status", "redirect_to_payment",
                    "paymentUrl", result,
                    "message", "Redirect to VNPay for payment"
                ));
            } else {
                // COD or other payment methods
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Order placed successfully"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get user order history", description = "Retrieve all orders for authenticated user")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders(@AuthenticationPrincipal User user) {
        try {
            List<OrderDTO> orders = orderService.getUserOrders(user);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get order details", description = "Get detailed information about a specific order")
    @ApiResponse(responseCode = "200", description = "Order details retrieved successfully")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderDetails(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {
        try {
            OrderDTO order = orderService.getOrderById(user, orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel order", description = "Cancel a pending order")
    @ApiResponse(responseCode = "200", description = "Order cancelled successfully")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Object> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {
        try {
            orderService.cancelOrder(user, orderId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Order cancelled successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @Operation(summary = "VNPay payment callback", description = "Handle VNPay payment result")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    @GetMapping("/vnpay/callback")
    public ResponseEntity<String> vnpayCallback(
            @RequestParam Map<String, String> params) {
        
        System.out.println("💳 [VNPAY CALLBACK] Received payment callback");
        System.out.println("📝 [VNPAY CALLBACK] Params: " + params);
        
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String amount = params.get("vnp_Amount");
        String orderInfo = params.get("vnp_OrderInfo");
        
        String html;
        
        if ("00".equals(responseCode)) {
            System.out.println("✅ [VNPAY CALLBACK] Payment successful - TxnRef: " + txnRef);
            
            // ✅ Update order status to COMPLETED and clear cart
            try {
                orderService.updateOrderStatusByTxnRef(txnRef);
                System.out.println("✅ [VNPAY CALLBACK] Order updated and cart cleared");
            } catch (Exception e) {
                System.err.println("❌ [VNPAY CALLBACK] Failed to update order: " + e.getMessage());
            }
            
            html = "<!DOCTYPE html>" +
                   "<html><head>" +
                   "<meta charset='UTF-8'>" +
                   "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                   "<title>Thanh toán thành công</title>" +
                   "<style>" +
                   "body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #f5f5f5; }" +
                   ".container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 500px; margin: 0 auto; }" +
                   ".success-icon { font-size: 80px; color: #4CAF50; margin-bottom: 20px; }" +
                   "h1 { color: #4CAF50; margin-bottom: 10px; }" +
                   "p { color: #666; margin: 10px 0; }" +
                   ".detail { background: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                   ".detail-row { display: flex; justify-content: space-between; margin: 8px 0; }" +
                   ".label { color: #888; }" +
                   ".value { font-weight: bold; color: #333; }" +
                   ".btn { display: inline-block; padding: 12px 30px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; cursor: pointer; border: none; }" +
                   "</style>" +
                   "</head><body>" +
                   "<div class='container'>" +
                   "<div class='success-icon'>✅</div>" +
                   "<h1>Thanh toán thành công!</h1>" +
                   "<p>Đơn hàng của bạn đã được xác nhận</p>" +
                   "<div class='detail'>" +
                   "<div class='detail-row'><span class='label'>Mã giao dịch:</span><span class='value'>" + txnRef + "</span></div>" +
                   "<div class='detail-row'><span class='label'>Số tiền:</span><span class='value'>" + (amount != null ? String.format("%,.0f VNĐ", Double.parseDouble(amount)/100) : "N/A") + "</span></div>" +
                   "<div class='detail-row'><span class='label'>Thông tin:</span><span class='value'>" + (orderInfo != null ? orderInfo : "N/A") + "</span></div>" +
                   "</div>" +
                   "<p style='color: #888; font-size: 14px;'>Vui lòng quay lại app để xem đơn hàng</p>" +
                   "<p style='color: #4CAF50; font-weight: bold;'>Bạn có thể đóng trang này</p>" +
                   "</div>" +
                   "</body></html>";
        } else {
            System.out.println("❌ [VNPAY CALLBACK] Payment failed - Code: " + responseCode + " - TxnRef: " + txnRef);
            
            html = "<!DOCTYPE html>" +
                   "<html><head>" +
                   "<meta charset='UTF-8'>" +
                   "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                   "<title>Thanh toán thất bại</title>" +
                   "<style>" +
                   "body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #f5f5f5; }" +
                   ".container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 500px; margin: 0 auto; }" +
                   ".error-icon { font-size: 80px; color: #f44336; margin-bottom: 20px; }" +
                   "h1 { color: #f44336; margin-bottom: 10px; }" +
                   "p { color: #666; margin: 10px 0; }" +
                   ".detail { background: #fff3f3; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: left; border-left: 4px solid #f44336; }" +
                   ".btn { display: inline-block; padding: 12px 30px; background: #f44336; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }" +
                   "</style>" +
                   "<script>" +
                   "setTimeout(function() { " +
                   "  var userAgent = navigator.userAgent.toLowerCase();" +
                   "  if (userAgent.indexOf('nike_fe') > -1) {" +
                   "    window.location.href = 'nike://payment-failed';" +
                   "  }" +
                   "}, 3000);" +
                   "</script>" +
                   "</head><body>" +
                   "<div class='container'>" +
                   "<div class='error-icon'>❌</div>" +
                   "<h1>Thanh toán thất bại</h1>" +
                   "<p>Giao dịch không thành công</p>" +
                   "<div class='detail'>" +
                   "<p><strong>Mã lỗi:</strong> " + responseCode + "</p>" +
                   "<p><strong>Mã giao dịch:</strong> " + txnRef + "</p>" +
                   "</div>" +
                   "<p style='color: #888; font-size: 14px;'>Vui lòng thử lại hoặc chọn phương thức thanh toán khác</p>" +
                   "<a href='intent://payment-failed#Intent;scheme=nike;package=com.example.nike_fe;end' class='btn'>Quay lại App</a>" +
                   "</div>" +
                   "</body></html>";
        }
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }
}