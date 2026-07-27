package com.proj.webprojrct.promotion.controller;

import com.proj.webprojrct.promotion.dto.request.CouponCreateRequest;
import com.proj.webprojrct.promotion.dto.request.CouponUpdateRequest;
import com.proj.webprojrct.promotion.dto.response.CouponResponse;
import com.proj.webprojrct.promotion.entity.Coupon;
import com.proj.webprojrct.promotion.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponRestController {

    private final CouponService couponService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort // Android sends "id,desc"
    ) {
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        String direction = sortParams.length > 1 ? sortParams[1] : "desc";

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(couponService.getAllCoupons(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody Map<String, Object> payload) {
        CouponCreateRequest request = mapToCreateRequest(payload);
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> updateCoupon(@PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        CouponUpdateRequest request = mapToUpdateRequest(payload);
        return ResponseEntity.ok(couponService.updateCoupon(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }

    private CouponCreateRequest mapToCreateRequest(Map<String, Object> payload) {
        CouponCreateRequest request = new CouponCreateRequest();
        request.setCode((String) payload.get("code"));
        
        // Nhận name từ payload, nếu không có thì dùng code
        String name = (String) payload.get("name");
        request.setName(name != null ? name : (String) payload.get("code"));
        
        request.setDescription((String) payload.get("description"));

        String type = (String) payload.get("discountType");
        if ("PERCENTAGE".equals(type) || "PERCENT".equals(type)) {
            request.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        } else {
            request.setDiscountType(Coupon.DiscountType.FIXED_AMOUNT);
        }

        Object val = payload.get("discountValue");
        request.setDiscountValue(parseDouble(val));

        Object maxDiscount = payload.get("maxDiscountAmount");
        request.setMaxDiscountAmount(parseDouble(maxDiscount));

        Object minOrder = payload.get("minOrderValue");
        request.setMinOrderAmount(parseDouble(minOrder));

        Object limit = payload.get("usageLimit");
        request.setUsageLimit(parseInteger(limit));

        request.setStartDate(parseDate((String) payload.get("startDate")));
        request.setEndDate(parseDate((String) payload.get("endDate")));

        // Nhận cả active và isActive
        Boolean isActive = (Boolean) payload.get("active");
        if (isActive == null) {
            isActive = (Boolean) payload.get("isActive");
        }
        request.setIsActive(isActive);

        return request;
    }

    private CouponUpdateRequest mapToUpdateRequest(Map<String, Object> payload) {
        CouponUpdateRequest request = new CouponUpdateRequest();
        
        // Nhận name từ payload
        String name = (String) payload.get("name");
        request.setName(name != null ? name : (String) payload.get("code"));
        
        request.setDescription((String) payload.get("description"));

        String type = (String) payload.get("discountType");
        if ("PERCENTAGE".equals(type) || "PERCENT".equals(type)) {
            request.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        } else {
            request.setDiscountType(Coupon.DiscountType.FIXED_AMOUNT);
        }

        Object val = payload.get("discountValue");
        request.setDiscountValue(parseDouble(val));

        Object maxDiscount = payload.get("maxDiscountAmount");
        request.setMaxDiscountAmount(parseDouble(maxDiscount));

        Object minOrder = payload.get("minOrderValue");
        request.setMinOrderAmount(parseDouble(minOrder));

        Object limit = payload.get("usageLimit");
        request.setUsageLimit(parseInteger(limit));

        request.setStartDate(parseDate((String) payload.get("startDate")));
        request.setEndDate(parseDate((String) payload.get("endDate")));

        // Nhận cả active và isActive
        Boolean isActive = (Boolean) payload.get("active");
        if (isActive == null) {
            isActive = (Boolean) payload.get("isActive");
        }
        request.setIsActive(isActive);

        return request;
    }

    private Double parseDouble(Object value) {
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        if (value instanceof String)
            return Double.parseDouble((String) value);
        return 0.0;
    }

    private Integer parseInteger(Object value) {
        if (value instanceof Number)
            return ((Number) value).intValue();
        if (value instanceof String)
            return Integer.parseInt((String) value);
        return 0;
    }

    private java.time.LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty())
            return null;
        try {
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);
            return date.atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }
}
