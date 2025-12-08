package com.proj.webprojrct.promotion.mapper;

import com.proj.webprojrct.promotion.dto.request.CouponCreateRequest;
import com.proj.webprojrct.promotion.dto.request.CouponUpdateRequest;
import com.proj.webprojrct.promotion.dto.response.CouponResponse;
import com.proj.webprojrct.promotion.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CouponMapper {

    // Convert entity to response
    @Mapping(target = "isValid", expression = "java(coupon.isValid())")
    CouponResponse toResponse(Coupon coupon);

    // Convert create request to entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usedCount", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Coupon toEntity(CouponCreateRequest request);

    // Update entity from update request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Code không được thay đổi
    @Mapping(target = "usedCount", ignore = true) // Used count không được thay đổi trực tiếp
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CouponUpdateRequest request, @MappingTarget Coupon coupon);
}