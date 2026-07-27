package com.proj.webprojrct.loyalty.controller;

import com.proj.webprojrct.common.util.SecurityUtil;
import com.proj.webprojrct.loyalty.dto.CheckinResponse;
import com.proj.webprojrct.loyalty.dto.CheckinStreakResponse;
import com.proj.webprojrct.loyalty.dto.LoyaltyPointsResponse;
import com.proj.webprojrct.loyalty.dto.TransactionHistoryResponse;
import com.proj.webprojrct.loyalty.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LoyaltyController - REST API cho Nike Coin / Loyalty Points
 */
@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    /**
     * Lấy thông tin loyalty points của user hiện tại
     * GET /api/v1/loyalty/points
     */
    @GetMapping("/points")
    public ResponseEntity<LoyaltyPointsResponse> getMyPoints() {
        Long userId = SecurityUtil.getCurrentUserId();
        LoyaltyPointsResponse response = loyaltyService.getLoyaltyPoints(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin checkin streak
     * GET /api/v1/loyalty/checkin/streak
     */
    @GetMapping("/checkin/streak")
    public ResponseEntity<CheckinStreakResponse> getCheckinStreak() {
        Long userId = SecurityUtil.getCurrentUserId();
        CheckinStreakResponse response = loyaltyService.getCheckinStreak(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Thực hiện checkin hàng ngày
     * POST /api/v1/loyalty/checkin
     */
    @PostMapping("/checkin")
    public ResponseEntity<CheckinResponse> performCheckin() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            CheckinResponse response = loyaltyService.performCheckin(userId);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                CheckinResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Lấy lịch sử giao dịch coin
     * GET /api/v1/loyalty/transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactionHistory() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<TransactionHistoryResponse> transactions = loyaltyService.getTransactionHistory(userId);
        return ResponseEntity.ok(transactions);
    }
}
