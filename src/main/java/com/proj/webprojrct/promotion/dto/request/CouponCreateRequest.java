package com.proj.webprojrct.promotion.dto.request;

import com.proj.webprojrct.promotion.entity.Coupon;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(min = 3, max = 50, message = "Mã giảm giá phải từ 3-50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã giảm giá chỉ được chứa chữ hoa, số, dấu _ và -")
    private String code;

    @NotBlank(message = "Tên coupon không được để trống")
    @Size(max = 100, message = "Tên coupon không được quá 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;

    @NotNull(message = "Loại giảm giá không được để trống")
    private Coupon.DiscountType discountType;

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm giá phải lớn hơn 0")
    private Double discountValue;

    @DecimalMin(value = "0.0", message = "Số tiền đơn hàng tối thiểu phải >= 0")
    private Double minOrderAmount = 0.0;

    private Double maxDiscountAmount;

    @Min(value = 1, message = "Số lần sử dụng phải >= 1")
    private Integer usageLimit = 1;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean isActive = true;

    // Custom validation
    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null values
        }
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Với loại giảm theo %, giá trị phải từ 1-100")
    public boolean isValidPercentageDiscount() {
        if (discountType == null || discountValue == null) {
            return true; // Let @NotNull handle null values
        }
        if (discountType == Coupon.DiscountType.PERCENTAGE) {
            return discountValue >= 1 && discountValue <= 100;
        }
        return true;
    }
}