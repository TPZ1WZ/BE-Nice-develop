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
            
            String result = orderService.placeOrder(
                user, 
                request.getReceiverName(),
                request.getShippingAddress(), 
                request.getPaymentMethod(), 
                request.getPhone(), 
                request.getCouponCode()
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
        // TODO: Implement VNPay callback handling
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        
        if ("00".equals(responseCode)) {
            // Payment successful
            // Update order status to COMPLETED
            return ResponseEntity.ok("Payment successful. Order confirmed.");
        } else {
            // Payment failed
            // Update order status to CANCELED or PENDING
            return ResponseEntity.ok("Payment failed. Please try again.");
        }
    }
}