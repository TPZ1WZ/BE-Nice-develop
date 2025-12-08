package com.proj.webprojrct.promotion.service;

import com.proj.webprojrct.promotion.dto.request.ApplyCouponRequest;
import com.proj.webprojrct.promotion.dto.request.CouponCreateRequest;
import com.proj.webprojrct.promotion.dto.request.CouponUpdateRequest;
import com.proj.webprojrct.promotion.dto.response.CouponDiscountResponse;
import com.proj.webprojrct.promotion.dto.response.CouponResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponService {

    // CRUD operations
    CouponResponse createCoupon(CouponCreateRequest request);
    
    CouponResponse getCouponById(Long id);
    
    CouponResponse getCouponByCode(String code);
    
    Page<CouponResponse> getAllCoupons(Pageable pageable);
    
    Page<CouponResponse> searchCoupons(String keyword, Pageable pageable);
    
    CouponResponse updateCoupon(Long id, CouponUpdateRequest request);
    
    void deleteCoupon(Long id);
    
    void toggleCouponStatus(Long id);

    // Business operations
    CouponDiscountResponse applyCoupon(ApplyCouponRequest request);
    
    void incrementUsedCount(String couponCode);
    
    List<CouponResponse> getValidCouponsList();
    
    List<CouponResponse> getExpiredCoupons();

    // Admin operations
    void cleanupExpiredCoupons();
    
    // Statistics
    Long getTotalCoupons();
    
    Long getActiveCoupons();
    
    Long getValidCouponsCount();
}