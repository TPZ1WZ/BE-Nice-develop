package com.proj.webprojrct.luckywheel.controller;

import com.proj.webprojrct.common.util.SecurityUtil;
import com.proj.webprojrct.luckywheel.entity.Prize;
import com.proj.webprojrct.luckywheel.entity.SpinHistory;
import com.proj.webprojrct.luckywheel.entity.UserCoupon;
import com.proj.webprojrct.luckywheel.entity.WheelConfig;
import com.proj.webprojrct.luckywheel.service.LuckyWheelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lucky-wheel")
@RequiredArgsConstructor
public class LuckyWheelController {

    private final LuckyWheelService luckyWheelService;

    /**
     * Lấy cấu hình vòng quay
     * GET /api/v1/lucky-wheel/config
     */
    @GetMapping("/config")
    public ResponseEntity<WheelConfig> getConfig() {
        return ResponseEntity.ok(luckyWheelService.getWheelConfig());
    }

    /**
     * Lấy danh sách phần thưởng
     * GET /api/v1/lucky-wheel/prizes
     */
    @GetMapping("/prizes")
    public ResponseEntity<List<Prize>> getPrizes() {
        return ResponseEntity.ok(luckyWheelService.getAvailablePrizes());
    }

    /**
     * Quay thưởng
     * POST /api/v1/lucky-wheel/spin
     */
    @PostMapping("/spin")
    public ResponseEntity<?> spin() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            SpinHistory result = luckyWheelService.spin(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("spinHistory", result);
            response.put("prize", result.getPrize());
            response.put("prizeCode", result.getPrizeCode());
            response.put("message", "Chúc mừng! Bạn đã nhận được " + result.getPrize().getName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Lấy số lượt quay còn lại hôm nay
     * GET /api/v1/lucky-wheel/remaining-spins
     */
    @GetMapping("/remaining-spins")
    public ResponseEntity<Map<String, Integer>> getRemainingSpins() {
        Long userId = SecurityUtil.getCurrentUserId();
        Integer remaining = luckyWheelService.getRemainingSpinsToday(userId);
        
        Map<String, Integer> response = new HashMap<>();
        response.put("remainingSpins", remaining);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy lịch sử quay của user
     * GET /api/v1/lucky-wheel/history?page=0&size=10
     */
    @GetMapping("/history")
    public ResponseEntity<Page<SpinHistory>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        Page<SpinHistory> history = luckyWheelService.getUserSpinHistory(
                userId, PageRequest.of(page, size));
        
        return ResponseEntity.ok(history);
    }

    /**
     * Nhận thưởng
     * POST /api/v1/lucky-wheel/claim/{historyId}
     */
    @PostMapping("/claim/{historyId}")
    public ResponseEntity<?> claimPrize(@PathVariable Long historyId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            SpinHistory history = luckyWheelService.claimPrize(historyId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã nhận thưởng thành công");
            response.put("spinHistory", history);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách phần thưởng đã nhận
     * GET /api/v1/lucky-wheel/my-prizes
     */
    @GetMapping("/my-prizes")
    public ResponseEntity<List<SpinHistory>> getMyPrizes() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<SpinHistory> myPrizes = luckyWheelService.getUserWonPrizes(userId);
        return ResponseEntity.ok(myPrizes);
    }

    /**
     * Lấy danh sách phần thưởng hấp dẫn (còn nhiều và xác suất cao)
     * GET /api/v1/lucky-wheel/attractive-prizes
     */
    @GetMapping("/attractive-prizes")
    public ResponseEntity<List<Prize>> getAttractivePrizes() {
        List<Prize> attractivePrizes = luckyWheelService.getAttractivePrizes();
        return ResponseEntity.ok(attractivePrizes);
    }

    /**
     * Lưu voucher vào ví
     * POST /api/v1/lucky-wheel/save-to-wallet/{historyId}
     */
    @PostMapping("/save-to-wallet/{historyId}")
    public ResponseEntity<?> saveToWallet(@PathVariable Long historyId) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            UserCoupon savedCoupon = luckyWheelService.saveToWallet(historyId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã lưu voucher vào ví thành công");
            response.put("userCoupon", savedCoupon);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Lấy danh sách voucher trong ví (còn hợp lệ)
     * GET /api/v1/lucky-wheel/my-wallet
     */
    @GetMapping("/my-wallet")
    public ResponseEntity<List<UserCoupon>> getMyWallet() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<UserCoupon> wallet = luckyWheelService.getMyWallet(userId);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Lấy tất cả voucher của user (cả đã dùng và hết hạn)
     * GET /api/v1/lucky-wheel/all-vouchers
     */
    @GetMapping("/all-vouchers")
    public ResponseEntity<List<UserCoupon>> getAllMyVouchers() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<UserCoupon> vouchers = luckyWheelService.getAllMyVouchers(userId);
        return ResponseEntity.ok(vouchers);
    }
}
