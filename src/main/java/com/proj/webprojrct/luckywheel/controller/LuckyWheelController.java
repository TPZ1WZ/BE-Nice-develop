package com.proj.webprojrct.luckywheel.controller;

import com.proj.webprojrct.common.util.SecurityUtil;
import com.proj.webprojrct.luckywheel.dto.LuckyWheelInfoResponse;
import com.proj.webprojrct.luckywheel.dto.SpinRequest;
import com.proj.webprojrct.luckywheel.dto.SpinResponse;
import com.proj.webprojrct.luckywheel.service.LuckyWheelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * LuckyWheelController - REST API cho vòng quay may mắn
 */
@RestController
@RequestMapping("/api/v1/lucky-wheel")
@RequiredArgsConstructor
@Slf4j
public class LuckyWheelController {

    private final LuckyWheelService luckyWheelService;

    /**
     * Lấy thông tin vòng quay
     * GET /api/v1/lucky-wheel/info
     */
    @GetMapping("/info")
    public ResponseEntity<LuckyWheelInfoResponse> getWheelInfo() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            LuckyWheelInfoResponse response = luckyWheelService.getWheelInfo(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get wheel info", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Thực hiện quay thưởng
     * POST /api/v1/lucky-wheel/spin
     */
    @PostMapping("/spin")
    public ResponseEntity<SpinResponse> performSpin(@RequestBody(required = false) SpinRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            SpinResponse response = luckyWheelService.performSpin(userId, true);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Failed to perform spin", e);
            return ResponseEntity.badRequest().body(
                SpinResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Ghi nhận lượt xem sản phẩm (để kiếm lượt quay)
     * POST /api/v1/lucky-wheel/track-product/{productId}
     */
    @PostMapping("/track-product/{productId}")
    public ResponseEntity<Map<String, Object>> trackProductView(@PathVariable Long productId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            luckyWheelService.recordProductView(userId, productId);
            
            // Lấy thông tin cập nhật sau khi track
            LuckyWheelInfoResponse info = luckyWheelService.getWheelInfo(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã ghi nhận lượt xem sản phẩm");
            response.put("productsViewedToday", info.getProductsViewedToday());
            response.put("requiredProductViews", info.getRequiredProductViews());
            response.put("canSpin", info.getHasFreeSpinToday());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to track product view", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
