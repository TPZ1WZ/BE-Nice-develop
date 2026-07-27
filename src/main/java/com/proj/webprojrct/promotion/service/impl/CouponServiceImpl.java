package com.proj.webprojrct.promotion.service.impl;

import com.proj.webprojrct.common.exception.EntityNotExistException;
import com.proj.webprojrct.promotion.dto.request.ApplyCouponRequest;
import com.proj.webprojrct.promotion.dto.request.CouponCreateRequest;
import com.proj.webprojrct.promotion.dto.request.CouponUpdateRequest;
import com.proj.webprojrct.promotion.dto.response.CouponDiscountResponse;
import com.proj.webprojrct.promotion.dto.response.CouponResponse;
import com.proj.webprojrct.promotion.entity.Coupon;
import com.proj.webprojrct.promotion.mapper.CouponMapper;
import com.proj.webprojrct.promotion.repository.CouponRepository;
import com.proj.webprojrct.promotion.service.CouponService;
import com.proj.webprojrct.notification.service.NotificationService;
import com.proj.webprojrct.notification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;
    private final NotificationService notificationService;

    @Override
    public CouponResponse createCoupon(CouponCreateRequest request) {
        log.info("Creating coupon with code: {}", request.getCode());
        
        // Kiểm tra code đã tồn tại
        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã coupon đã tồn tại: " + request.getCode());
        }

        Coupon coupon = couponMapper.toEntity(request);
        coupon = couponRepository.save(coupon);
        
        log.info("Created coupon successfully with ID: {}", coupon.getId());
        
        // Gửi thông báo broadcast cho tất cả users
        try {
            String message = String.format("%s. HSD: %s", 
                request.getDescription() != null ? request.getDescription() : request.getName(),
                request.getEndDate() != null ? request.getEndDate().toString() : "Không giới hạn"
            );
            
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("couponCode", coupon.getCode());
            data.put("discount", request.getDiscountValue());
            data.put("discountType", request.getDiscountType());
            data.put("endDate", request.getEndDate());
            
            notificationService.broadcastNotification(
                NotificationType.COUPON,
                "Mã giảm giá mới: " + coupon.getCode(),
                message,
                data
            );
            log.info("Broadcast notification sent for new coupon: {}", coupon.getCode());
        } catch (Exception e) {
            log.error("Failed to send coupon notification: {}", e.getMessage());
        }
        
        return couponMapper.toResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long id) {
        Coupon coupon = findCouponById(id);
        return couponMapper.toResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotExistException("Không tìm thấy coupon với mã: " + code));
        return couponMapper.toResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        // Chỉ trả về coupon còn active (chưa bị xóa soft delete)
        return couponRepository.findAll(pageable)
                .map(couponMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> searchCoupons(String keyword, Pageable pageable) {
        return couponRepository.findByNameOrCodeContaining(keyword, pageable)
                .map(couponMapper::toResponse);
    }

    @Override
    public CouponResponse updateCoupon(Long id, CouponUpdateRequest request) {
        log.info("Updating coupon with ID: {}", id);
        
        Coupon coupon = findCouponById(id);
        
        // Validate business rules
        if (request.getUsageLimit() != null && request.getUsageLimit() < coupon.getUsedCount()) {
            throw new IllegalArgumentException("Số lần sử dụng không thể nhỏ hơn số lần đã sử dụng (" + coupon.getUsedCount() + ")");
        }

        couponMapper.updateEntity(request, coupon);
        coupon = couponRepository.save(coupon);
        
        log.info("Updated coupon successfully with ID: {}", coupon.getId());
        return couponMapper.toResponse(coupon);
    }

    @Override
    public void deleteCoupon(Long id) {
        log.info("Deleting coupon with ID: {}", id);
        
        Coupon coupon = findCouponById(id);
        
        // Hard delete - xóa hẳn khỏi database
        couponRepository.delete(coupon);
        
        log.info("Deleted coupon successfully with ID: {}", id);
    }

    @Override
    public void toggleCouponStatus(Long id) {
        log.info("Toggling coupon status with ID: {}", id);
        
        Coupon coupon = findCouponById(id);
        coupon.setIsActive(!coupon.getIsActive());
        couponRepository.save(coupon);
        
        log.info("Toggled coupon status to {} for ID: {}", coupon.getIsActive(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponDiscountResponse applyCoupon(ApplyCouponRequest request) {
        log.info("Applying coupon: {} for order amount: {}", request.getCouponCode(), request.getOrderAmount());
        
        // Tìm coupon
        Coupon coupon = couponRepository.findByCodeAndIsActive(request.getCouponCode(), true)
                .orElse(null);

        if (coupon == null) {
            return CouponDiscountResponse.invalid("Mã giảm giá không tồn tại hoặc đã bị vô hiệu hóa");
        }

        // Kiểm tra coupon có thể áp dụng
        if (!coupon.canApplyToOrder(request.getOrderAmount())) {
            if (!coupon.isValid()) {
                return CouponDiscountResponse.invalid("Mã giảm giá đã hết hạn hoặc đã sử dụng hết");
            }
            if (request.getOrderAmount() < coupon.getMinOrderAmount()) {
                return CouponDiscountResponse.invalid("Đơn hàng chưa đạt giá trị tối thiểu: " + 
                    String.format("%,.0f đ", coupon.getMinOrderAmount()));
            }
        }

        // Tính toán giảm giá
        Double discountAmount = coupon.calculateDiscount(request.getOrderAmount());
        Double finalAmount = request.getOrderAmount() - discountAmount;

        CouponResponse couponResponse = couponMapper.toResponse(coupon);
        
        log.info("Applied coupon successfully. Discount: {}, Final: {}", discountAmount, finalAmount);
        return CouponDiscountResponse.valid(discountAmount, finalAmount, couponResponse);
    }

    @Override
    public void incrementUsedCount(String couponCode) {
        log.info("Incrementing used count for coupon: {}", couponCode);
        
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new EntityNotExistException("Không tìm thấy coupon với mã: " + couponCode));
        
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
        
        log.info("Incremented used count to {} for coupon: {}", coupon.getUsedCount(), couponCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getValidCouponsList() {
        // Use findValidCouponsV2 which handles NULL dates correctly
        // Pass null for orderAmount to get all valid coupons regardless of order amount
        return couponRepository.findValidCouponsV2(null)
                .stream()
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getExpiredCoupons() {
        return couponRepository.findExpiredCoupons(LocalDateTime.now())
                .stream()
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void cleanupExpiredCoupons() {
        log.info("Cleaning up expired coupons");
        
        List<Coupon> expiredCoupons = couponRepository.findExpiredCoupons(LocalDateTime.now());
        expiredCoupons.forEach(coupon -> coupon.setIsActive(false));
        couponRepository.saveAll(expiredCoupons);
        
        log.info("Cleaned up {} expired coupons", expiredCoupons.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalCoupons() {
        return couponRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getActiveCoupons() {
        return couponRepository.countByIsActive(true);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getValidCouponsCount() {
        return couponRepository.countValidCoupons(LocalDateTime.now());
    }

    private Coupon findCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotExistException("Không tìm thấy coupon với ID: " + id));
    }
}