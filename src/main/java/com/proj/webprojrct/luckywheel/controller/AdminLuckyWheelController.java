package com.proj.webprojrct.luckywheel.controller;

import com.proj.webprojrct.luckywheel.dto.BatchProbabilityRequest;
import com.proj.webprojrct.luckywheel.dto.LuckyWheelStatistics;
import com.proj.webprojrct.luckywheel.dto.UserSpinManagement;
import com.proj.webprojrct.luckywheel.entity.Prize;
import com.proj.webprojrct.luckywheel.entity.SpinHistory;
import com.proj.webprojrct.luckywheel.entity.WheelConfig;
import com.proj.webprojrct.luckywheel.service.LuckyWheelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/lucky-wheel")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLuckyWheelController {

    private final LuckyWheelService luckyWheelService;

    // ============== 1️⃣ QUẢN LÝ PHẦN THƯỞNG ==============

    /**
     * Lấy tất cả phần thưởng (kể cả inactive)
     * GET /api/v1/admin/lucky-wheel/prizes
     */
    @GetMapping("/prizes")
    public ResponseEntity<List<Prize>> getAllPrizes() {
        return ResponseEntity.ok(luckyWheelService.getAllPrizes());
    }

    /**
     * Tạo phần thưởng mới
     * POST /api/v1/admin/lucky-wheel/prizes
     */
    @PostMapping("/prizes")
    public ResponseEntity<Prize> createPrize(@RequestBody Prize prize) {
        Prize created = luckyWheelService.createPrize(prize);
        return ResponseEntity.ok(created);
    }

    /**
     * Cập nhật phần thưởng
     * PUT /api/v1/admin/lucky-wheel/prizes/{id}
     */
    @PutMapping("/prizes/{id}")
    public ResponseEntity<Prize> updatePrize(
            @PathVariable Long id,
            @RequestBody Prize prize) {
        Prize updated = luckyWheelService.updatePrize(id, prize);
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa phần thưởng
     * DELETE /api/v1/admin/lucky-wheel/prizes/{id}
     */
    @DeleteMapping("/prizes/{id}")
    public ResponseEntity<?> deletePrize(@PathVariable Long id) {
        luckyWheelService.deletePrize(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa phần thưởng"));
    }

    // ============== 2️⃣ CẤU HÌNH XÁC SUẤT ==============

    /**
     * Cập nhật xác suất nhiều phần thưởng cùng lúc
     * POST /api/v1/admin/lucky-wheel/prizes/batch-probability
     * 
     * Body: {
     *   "prizes": [
     *     {"prizeId": 1, "probability": 0.05},
     *     {"prizeId": 2, "probability": 0.20},
     *     {"prizeId": 3, "probability": 0.40}
     *   ]
     * }
     */
    @PostMapping("/prizes/batch-probability")
    public ResponseEntity<List<Prize>> batchUpdateProbabilities(@RequestBody BatchProbabilityRequest request) {
        List<Prize> updated = luckyWheelService.batchUpdateProbabilities(request);
        return ResponseEntity.ok(updated);
    }

    // ============== 3️⃣ QUẢN LÝ SỰ KIỆN & CẤU HÌNH ==============

    /**
     * Lấy cấu hình hiện tại
     * GET /api/v1/admin/lucky-wheel/config
     */
    @GetMapping("/config")
    public ResponseEntity<WheelConfig> getConfig() {
        return ResponseEntity.ok(luckyWheelService.getWheelConfig());
    }

    /**
     * Cập nhật cấu hình vòng quay
     * PUT /api/v1/admin/lucky-wheel/config
     */
    @PutMapping("/config")
    public ResponseEntity<WheelConfig> updateConfig(@RequestBody WheelConfig config) {
        WheelConfig updated = luckyWheelService.updateConfig(config);
        return ResponseEntity.ok(updated);
    }

    /**
     * Bật/tắt vòng quay
     * POST /api/v1/admin/lucky-wheel/toggle?active=true
     */
    @PostMapping("/toggle")
    public ResponseEntity<WheelConfig> toggleWheelStatus(@RequestParam Boolean active) {
        WheelConfig updated = luckyWheelService.toggleWheelStatus(active);
        return ResponseEntity.ok(updated);
    }

    /**
     * Cập nhật thời gian sự kiện
     * POST /api/v1/admin/lucky-wheel/event-schedule
     * 
     * Params:
     * - eventName: Tên sự kiện (vd: "Tết Nguyên Đán 2026")
     * - startDate: 2026-01-28T00:00:00
     * - endDate: 2026-02-03T23:59:59
     * - isTimeRestricted: true/false
     */
    @PostMapping("/event-schedule")
    public ResponseEntity<WheelConfig> updateEventSchedule(
            @RequestParam(required = false) String eventName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "false") Boolean isTimeRestricted) {
        
        WheelConfig updated = luckyWheelService.updateEventSchedule(eventName, startDate, endDate, isTimeRestricted);
        return ResponseEntity.ok(updated);
    }

    // ============== 4️⃣ QUẢN LÝ LƯỢT QUAY USER ==============

    /**
     * Xem danh sách user và lượt quay của họ
     * GET /api/v1/admin/lucky-wheel/user-spins
     */
    @GetMapping("/user-spins")
    public ResponseEntity<List<UserSpinManagement>> getUserSpinManagement() {
        List<UserSpinManagement> management = luckyWheelService.getUserSpinManagement();
        return ResponseEntity.ok(management);
    }

    /**
     * Reset lượt quay hôm nay của user (để user có thể quay lại)
     * POST /api/v1/admin/lucky-wheel/user-spins/{userId}/reset
     */
    @PostMapping("/user-spins/{userId}/reset")
    public ResponseEntity<?> resetUserSpins(@PathVariable Long userId) {
        luckyWheelService.resetUserSpinsToday(userId);
        return ResponseEntity.ok(Map.of(
            "success", true, 
            "message", "Đã reset lượt quay của user " + userId
        ));
    }

    /**
     * Tặng thêm lượt quay cho user
     * POST /api/v1/admin/lucky-wheel/user-spins/{userId}/grant?bonusSpins=5
     */
    @PostMapping("/user-spins/{userId}/grant")
    public ResponseEntity<?> grantBonusSpins(
            @PathVariable Long userId, 
            @RequestParam Integer bonusSpins) {
        
        luckyWheelService.grantBonusSpins(userId, bonusSpins);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã tặng " + bonusSpins + " lượt quay cho user " + userId
        ));
    }

    /**
     * Xem lịch sử quay của 1 user cụ thể
     * GET /api/v1/admin/lucky-wheel/user-spins/{userId}/history?page=0&size=20
     */
    @GetMapping("/user-spins/{userId}/history")
    public ResponseEntity<Page<SpinHistory>> getUserHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<SpinHistory> history = luckyWheelService.getUserSpinHistory(
                userId, PageRequest.of(page, size));
        
        return ResponseEntity.ok(history);
    }

    // ============== 5️⃣ XEM TẤT CẢ LỊCH SỬ ==============

    /**
     * Xem tất cả lịch sử quay (admin)
     * GET /api/v1/admin/lucky-wheel/history?page=0&size=20
     */
    @GetMapping("/history")
    public ResponseEntity<Page<SpinHistory>> getAllHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<SpinHistory> history = luckyWheelService.getAllSpinHistory(
                PageRequest.of(page, size));
        
        return ResponseEntity.ok(history);
    }

    // ============== 6️⃣ THỐNG KÊ ==============

    /**
     * Lấy thống kê tổng quan
     * GET /api/v1/admin/lucky-wheel/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<LuckyWheelStatistics> getStatistics() {
        LuckyWheelStatistics stats = luckyWheelService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}
