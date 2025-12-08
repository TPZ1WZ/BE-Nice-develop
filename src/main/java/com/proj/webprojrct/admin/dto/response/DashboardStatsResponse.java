package com.proj.webprojrct.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    
    // === TỔNG QUAN ===
    private BigDecimal totalRevenue;
    private BigDecimal revenueGrowth; // % tăng trưởng so với kỳ trước
    
    private Integer totalOrders;
    private Integer orderGrowth; // % tăng trưởng đơn hàng
    
    private Integer totalUsers;
    private Integer userGrowth; // % tăng trưởng user
    
    private Integer totalProducts;
    private Integer lowStockProducts; // Sản phẩm sắp hết hàng
    
    // === BIỂU ĐỒ DOANH THU ===
    private List<RevenueChartData> revenueChart;
    
    // === BIỂU ĐỒ ĐỚN HÀNG ===
    private List<OrderChartData> orderChart;
    
    // === TOP SẢN PHẨM BÁN CHẠY ===
    private List<TopProductData> topProducts;
    
    // === THỐNG KÊ ĐƠN HÀNG THEO TRẠNG THÁI ===
    private Map<String, Integer> orderStatusStats;
    
    // === THỐNG KÊ THANH TOÁN ===
    private Map<String, Integer> paymentMethodStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueChartData {
        private LocalDate date;
        private BigDecimal revenue;
        private String period; // DAY, WEEK, MONTH, YEAR
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderChartData {
        private LocalDate date;
        private Integer orderCount;
        private String period;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductData {
        private Long productId;
        private String productName;
        private String productImage;
        private Integer soldQuantity;
        private BigDecimal revenue;
    }
}