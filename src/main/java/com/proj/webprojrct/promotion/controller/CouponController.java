package com.proj.webprojrct.promotion.controller;

import com.proj.webprojrct.promotion.dto.request.ApplyCouponRequest;
import com.proj.webprojrct.promotion.dto.request.CouponCreateRequest;
import com.proj.webprojrct.promotion.dto.request.CouponUpdateRequest;
import com.proj.webprojrct.promotion.dto.response.CouponDiscountResponse;
import com.proj.webprojrct.promotion.dto.response.CouponResponse;
import com.proj.webprojrct.promotion.repository.CouponRepository;
import com.proj.webprojrct.promotion.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CouponController {

    private final CouponService couponService;
    private final CouponRepository couponRepository;

    // ===== CRUD Operations =====
    @GetMapping("available")
    public Object getAvailableCoupons(@RequestParam("amount") Double amount) {
        return couponRepository.findValidCouponsV2(amount);
    }

    @PostMapping
    public ResponseEntity<Object> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        try {
            CouponResponse response = couponService.createCoupon(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getCouponById(@PathVariable Long id) {
        try {
            CouponResponse response = couponService.getCouponById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Object> getCouponByCode(@PathVariable String code) {
        try {
            CouponResponse response = couponService.getCouponByCode(code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<CouponResponse> response;
        if (search != null && !search.trim().isEmpty()) {
            response = couponService.searchCoupons(search.trim(), pageable);
        } else {
            response = couponService.getAllCoupons(pageable);
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponUpdateRequest request) {
        try {
            CouponResponse response = couponService.updateCoupon(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            Map<String, String> success = new HashMap<>();
            success.put("message", "Xóa mã giảm giá thành công");
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Object> toggleCouponStatus(@PathVariable Long id) {
        try {
            couponService.toggleCouponStatus(id);
            Map<String, String> success = new HashMap<>();
            success.put("message", "Thay đổi trạng thái mã giảm giá thành công");
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ===== Business Operations =====

    @PostMapping("/apply")
    public ResponseEntity<CouponDiscountResponse> applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        try {
            CouponDiscountResponse response = couponService.applyCoupon(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(CouponDiscountResponse.invalid(e.getMessage()));
        }
    }

    @PostMapping("/{code}/use")
    public ResponseEntity<Object> useCoupon(@PathVariable String code) {
        try {
            couponService.incrementUsedCount(code);
            Map<String, String> success = new HashMap<>();
            success.put("message", "Sử dụng mã giảm giá thành công");
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/valid")
    public ResponseEntity<List<CouponResponse>> getValidCoupons() {
        List<CouponResponse> response = couponService.getValidCouponsList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<CouponResponse>> getExpiredCoupons() {
        List<CouponResponse> response = couponService.getExpiredCoupons();
        return ResponseEntity.ok(response);
    }

    // ===== Admin Operations =====

    @PostMapping("/cleanup-expired")
    public ResponseEntity<Object> cleanupExpiredCoupons() {
        try {
            couponService.cleanupExpiredCoupons();
            Map<String, String> success = new HashMap<>();
            success.put("message", "Dọn dẹp mã giảm giá hết hạn thành công");
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ===== Statistics =====

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCouponStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", couponService.getTotalCoupons());
        statistics.put("active", couponService.getActiveCoupons());
        statistics.put("valid", couponService.getValidCouponsCount());
        
        return ResponseEntity.ok(statistics);
    }
}