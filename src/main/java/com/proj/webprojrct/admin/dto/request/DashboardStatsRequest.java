package com.proj.webprojrct.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsRequest {
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;
    
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|YEARLY", message = "Loại thống kê không hợp lệ")
    private String timeFrame; // DAILY, WEEKLY, MONTHLY, YEARLY
    
    @Builder.Default
    private Boolean includeRevenue = true;
    
    @Builder.Default
    private Boolean includeOrders = true;
    
    @Builder.Default
    private Boolean includeUsers = true;
    
    @Builder.Default
    private Boolean includeProducts = true;
}