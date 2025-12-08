package com.proj.webprojrct.promotion.controller;

import com.proj.webprojrct.promotion.dto.request.CouponCreateRequest;
import com.proj.webprojrct.promotion.dto.request.CouponUpdateRequest;
import com.proj.webprojrct.promotion.dto.response.CouponResponse;
import com.proj.webprojrct.promotion.entity.Coupon;
import com.proj.webprojrct.promotion.entity.EDiscountType;
import com.proj.webprojrct.promotion.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public String listCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<CouponResponse> coupons;
        if (search != null && !search.trim().isEmpty()) {
            coupons = couponService.searchCoupons(search.trim(), pageable);
        } else {
            coupons = couponService.getAllCoupons(pageable);
        }
        
        model.addAttribute("coupons", coupons);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        
        // Statistics
        model.addAttribute("totalCoupons", couponService.getTotalCoupons());
        model.addAttribute("activeCoupons", couponService.getActiveCoupons());
        model.addAttribute("validCoupons", couponService.getValidCouponsCount());

        return "admin/coupons-minimal";
    }

    @GetMapping("/create")
    public String createCouponForm(Model model) {
        model.addAttribute("couponCreateRequest", new CouponCreateRequest());
        model.addAttribute("discountTypes", EDiscountType.values());
        model.addAttribute("pageTitle", "Tạo mã giảm giá mới");
        model.addAttribute("action", "create");
        return "admin/coupon-form-ultra";
    }

    @PostMapping("/create")
    public String createCoupon(@ModelAttribute("couponCreateRequest") CouponCreateRequest request, 
                               Model model) {
        try {
            couponService.createCoupon(request);
            return "redirect:/admin/coupons?success=created";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tạo mã giảm giá: " + e.getMessage());
            model.addAttribute("couponCreateRequest", request);
            model.addAttribute("discountTypes", EDiscountType.values());
            model.addAttribute("pageTitle", "Tạo mã giảm giá mới");
            model.addAttribute("action", "create");
            return "admin/coupon-form-ultra";
        }
    }

    @GetMapping("/{id}")
    public String viewCoupon(@PathVariable Long id, Model model) {
        try {
            CouponResponse coupon = couponService.getCouponById(id);
            model.addAttribute("coupon", coupon);
            return "admin/coupon-detail";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/coupons";
        }
    }

    @GetMapping("/edit/{id}")
    public String editCouponForm(@PathVariable Long id, Model model) {
        try {
            CouponResponse coupon = couponService.getCouponById(id);
            
            // Convert CouponResponse to CouponCreateRequest for form binding
            CouponCreateRequest couponRequest = new CouponCreateRequest();
            couponRequest.setCode(coupon.getCode());
            couponRequest.setName(coupon.getName());
            couponRequest.setDescription(coupon.getDescription());
            couponRequest.setDiscountType(coupon.getDiscountType());
            couponRequest.setDiscountValue(coupon.getDiscountValue());
            couponRequest.setMinOrderAmount(coupon.getMinOrderAmount());
            couponRequest.setMaxDiscountAmount(coupon.getMaxDiscountAmount());
            couponRequest.setUsageLimit(coupon.getUsageLimit());
            couponRequest.setStartDate(coupon.getStartDate());
            couponRequest.setEndDate(coupon.getEndDate());
            couponRequest.setIsActive(coupon.getIsActive());
            
            model.addAttribute("couponCreateRequest", couponRequest);
            model.addAttribute("coupon", coupon);
            model.addAttribute("discountTypes", EDiscountType.values());
            model.addAttribute("pageTitle", "Chỉnh sửa mã giảm giá: " + coupon.getCode());
            model.addAttribute("action", "edit");
            model.addAttribute("couponId", id);
            return "admin/coupon-form-ultra";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/coupons";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCoupon(@PathVariable Long id, 
                               @ModelAttribute("couponCreateRequest") CouponCreateRequest request,
                               Model model) {
        try {
            // Convert to CouponUpdateRequest
            CouponUpdateRequest updateRequest = new CouponUpdateRequest();
            updateRequest.setName(request.getName());
            updateRequest.setDescription(request.getDescription());
            updateRequest.setDiscountType(request.getDiscountType());
            updateRequest.setDiscountValue(request.getDiscountValue());
            updateRequest.setMinOrderAmount(request.getMinOrderAmount());
            updateRequest.setMaxDiscountAmount(request.getMaxDiscountAmount());
            updateRequest.setUsageLimit(request.getUsageLimit());
            updateRequest.setStartDate(request.getStartDate());
            updateRequest.setEndDate(request.getEndDate());
            updateRequest.setIsActive(request.getIsActive());
            
            couponService.updateCoupon(id, updateRequest);
            return "redirect:/admin/coupons?success=updated";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi cập nhật mã giảm giá: " + e.getMessage());
            model.addAttribute("couponCreateRequest", request);
            model.addAttribute("discountTypes", EDiscountType.values());
            model.addAttribute("pageTitle", "Chỉnh sửa mã giảm giá");
            model.addAttribute("action", "edit");
            model.addAttribute("couponId", id);
            return "admin/coupon-form-ultra";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleCouponStatus(@PathVariable Long id) {
        try {
            couponService.toggleCouponStatus(id);
        } catch (Exception e) {
            // Log error
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleCouponStatusAjax(@PathVariable Long id) {
        try {
            couponService.toggleCouponStatus(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thay đổi trạng thái: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
        } catch (Exception e) {
            // Log error
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCouponAjax(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa mã giảm giá: " + e.getMessage());
        }
    }
}