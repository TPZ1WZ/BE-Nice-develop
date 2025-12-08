package com.proj.webprojrct.admin.service;

import com.proj.webprojrct.admin.repository.AdminUserRepository;
import com.proj.webprojrct.admin.repository.AdminProductRepository;
import com.proj.webprojrct.admin.repository.AdminOrderRepository;
import com.proj.webprojrct.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service tổng hợp cho admin dashboard - xử lý logic phức tạp kết hợp nhiều domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AdminUserRepository adminUserRepository;
    private final AdminProductRepository adminProductRepository;
    private final AdminOrderRepository adminOrderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Lấy tổng quan dashboard với thống kê tổng hợp
     */
    public Map<String, Object> getDashboardOverview() {
        log.debug("Lấy tổng quan dashboard");
        
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // Thống kê cơ bản
            overview.put("totalUsers", adminUserRepository.count());
            overview.put("totalProducts", adminProductRepository.count());
            overview.put("totalOrders", adminOrderRepository.count());
            overview.put("totalReviews", 0);
            
            // Thống kê doanh thu
            Double totalRevenueDouble = adminOrderRepository.calculateTotalRevenue();
            BigDecimal totalRevenue = totalRevenueDouble != null ? 
                BigDecimal.valueOf(totalRevenueDouble) : BigDecimal.ZERO;
            overview.put("totalRevenue", totalRevenue);
            
            // TODO: Implement các thống kê chi tiết khi có methods trong repositories
            overview.put("monthlyRevenue", BigDecimal.ZERO); // Placeholder
            overview.put("newUsersThisMonth", 0L); // Placeholder
            overview.put("pendingOrders", 0L); // Placeholder
            
            log.info("Lấy tổng quan dashboard thành công");
            return overview;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy tổng quan dashboard", e);
            return Map.of("error", "Không thể lấy dữ liệu dashboard");
        }
    }

    /**
     * Lấy thống kê chi tiết cho từng module
     */
    public Map<String, Object> getDetailedStatistics() {
        log.debug("Lấy thống kê chi tiết");
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Thống kê người dùng
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("total", adminUserRepository.count());
            // TODO: Implement countUsersByRole() method
            userStats.put("byRole", Map.of()); // Placeholder
            userStats.put("activeUsers", adminUserRepository.count()); // Placeholder cho đến khi có method countActiveUsers()
            stats.put("users", userStats);
            
            // Thống kê sản phẩm  
            Map<String, Object> productStats = new HashMap<>();
            productStats.put("total", adminProductRepository.count());
            // TODO: Implement các method thống kê sản phẩm
            productStats.put("averagePrice", BigDecimal.ZERO); // Placeholder
            productStats.put("lowStock", List.of()); // Placeholder
            stats.put("products", productStats);
            
            // Thống kê đơn hàng
            Map<String, Object> orderStats = new HashMap<>();
            orderStats.put("total", adminOrderRepository.count());
            Double totalRevenueDouble = adminOrderRepository.calculateTotalRevenue();
            BigDecimal totalRevenue = totalRevenueDouble != null ? 
                BigDecimal.valueOf(totalRevenueDouble) : BigDecimal.ZERO;
            orderStats.put("totalRevenue", totalRevenue);
            // TODO: Implement countOrdersByStatus() method
            orderStats.put("byStatus", Map.of()); // Placeholder
            stats.put("orders", orderStats);
            
            // Thống kê đánh giá
            Map<String, Object> reviewStats = new HashMap<>();
            reviewStats.put("total",0);
            // TODO: Implement getAverageRating() method
            reviewStats.put("averageRating", 0.0); // Placeholder
            stats.put("reviews", reviewStats);
            
            log.info("Lấy thống kê chi tiết thành công");
            return stats;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê chi tiết", e);
            return Map.of("error", "Không thể lấy thống kê chi tiết");
        }
    }

    /**
     * Lấy dữ liệu cho biểu đồ dashboard
     */
    public Map<String, Object> getChartData(String chartType, Integer days) {
        log.debug("Lấy dữ liệu biểu đồ loại: {} cho {} ngày", chartType, days);
        
        Map<String, Object> chartData = new HashMap<>();
        
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            
            switch (chartType.toLowerCase()) {
                case "revenue":
                    List<Object[]> revenueData = adminOrderRepository.findRevenueByDateRange(startDate, endDate);
                    List<String> revenueLabels = new ArrayList<>();
                    List<Double> revenueValues = new ArrayList<>();
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    for (Object[] row : revenueData) {
                        LocalDate date = (LocalDate) row[0];
                        Double revenue = (Double) row[1];
                        revenueLabels.add(date.format(formatter));
                        revenueValues.add(revenue != null ? revenue : 0.0);
                    }
                    
                    chartData.put("labels", revenueLabels);
                    chartData.put("data", revenueValues);
                    chartData.put("title", "Doanh thu " + days + " ngày qua");
                    break;
                    
                case "orders":
                    List<Object[]> orderData = adminOrderRepository.findOrderCountByDateRange(startDate, endDate);
                    List<String> orderLabels = new ArrayList<>();
                    List<Long> orderCounts = new ArrayList<>();
                    
                    DateTimeFormatter orderFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    for (Object[] row : orderData) {
                        LocalDate date = (LocalDate) row[0];
                        Long count = (Long) row[1];
                        orderLabels.add(date.format(orderFormatter));
                        orderCounts.add(count != null ? count : 0L);
                    }
                    
                    chartData.put("labels", orderLabels);
                    chartData.put("data", orderCounts);
                    chartData.put("title", "Đơn hàng " + days + " ngày qua");
                    break;
                    
                case "users":
                    // TODO: Implement getUserChartData() method
                    chartData.put("labels", List.of()); // Placeholder
                    chartData.put("data", List.of()); // Placeholder
                    chartData.put("title", "Người dùng mới " + days + " ngày qua");
                    break;
                    
                default:
                    chartData.put("error", "Loại biểu đồ không được hỗ trợ: " + chartType);
            }
            
            return chartData;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy dữ liệu biểu đồ {}", chartType, e);
            return Map.of("error", "Không thể lấy dữ liệu biểu đồ");
        }
    }

    /**
     * Lấy hoạt động gần đây từ tất cả modules
     */
    public Map<String, Object> getRecentActivities(int limit) {
        log.debug("Lấy {} hoạt động gần đây", limit);
        
        Map<String, Object> activities = new HashMap<>();
        
        try {
            // TODO: Implement các method getRecent*() trong repositories
            activities.put("recentUsers", List.of()); // Placeholder
            activities.put("recentOrders", List.of()); // Placeholder
            activities.put("recentProducts", List.of()); // Placeholder
            activities.put("recentReviews", List.of()); // Placeholder
            
            log.info("Lấy hoạt động gần đây thành công");
            return activities;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy hoạt động gần đây", e);
            return Map.of("error", "Không thể lấy hoạt động gần đây");
        }
    }

    /**
     * Lấy cảnh báo và thông báo quan trọng
     */
    public Map<String, Object> getSystemAlerts() {
        log.debug("Lấy cảnh báo hệ thống");
        
        Map<String, Object> alerts = new HashMap<>();
        
        try {
            // TODO: Implement các method kiểm tra cảnh báo
            alerts.put("lowStockProducts", List.of()); // Placeholder
            alerts.put("pendingOrders", List.of()); // Placeholder
            alerts.put("systemHealth", "good"); // Placeholder
            alerts.put("criticalIssues", List.of()); // Placeholder
            
            log.info("Lấy cảnh báo hệ thống thành công");
            return alerts;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy cảnh báo hệ thống", e);
            return Map.of("error", "Không thể lấy cảnh báo hệ thống");
        }
    }

    /**
     * Lấy phân bố trạng thái đơn hàng
     */
    public Map<String, Object> getOrderStatusDistribution() {
        log.debug("Lấy phân bố trạng thái đơn hàng");
        
        Map<String, Object> distribution = new HashMap<>();
        
        try {
            long completed = adminOrderRepository.countByStatus("completed");
            long pending = adminOrderRepository.countByStatus("pending");
            long confirmed = adminOrderRepository.countByStatus("confirmed");
            long shipping = adminOrderRepository.countByStatus("shipping");
            long canceled = adminOrderRepository.countByStatus("canceled");
            
            distribution.put("completed", completed);
            distribution.put("pending", pending);
            distribution.put("confirmed", confirmed);
            distribution.put("shipping", shipping);
            distribution.put("canceled", canceled);
            
            log.info("Lấy phân bố trạng thái đơn hàng thành công");
            return distribution;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy phân bố trạng thái đơn hàng", e);
            return Map.of("error", "Không thể lấy phân bố trạng thái đơn hàng");
        }
    }
    
    /**
     * Lấy danh sách sản phẩm bán chạy nhất
     */
    public Map<String, Object> getTopSellingProducts(int limit) {
        log.debug("Lấy {} sản phẩm bán chạy", limit);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Object[]> topProducts = orderItemRepository.findTopSellingProducts();
            List<Map<String, Object>> productList = new ArrayList<>();
            
            int count = 0;
            for (Object[] row : topProducts) {
                if (count >= limit) break;
                
                Map<String, Object> product = new HashMap<>();
                product.put("id", row[0]);
                product.put("name", row[1]);
                product.put("soldQuantity", ((Number) row[2]).intValue());
                
                productList.add(product);
                count++;
            }
            
            result.put("products", productList);
            result.put("total", topProducts.size());
            
            log.info("Lấy {} sản phẩm bán chạy thành công", productList.size());
            return result;
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm bán chạy", e);
            return Map.of("error", "Không thể lấy sản phẩm bán chạy", "products", List.of());
        }
    }

    /**
     * Tính toán KPI (Key Performance Indicators)
     */
    public Map<String, Object> calculateKPIs() {
        log.debug("Tính toán KPI");
        
        Map<String, Object> kpis = new HashMap<>();
        
        try {
            // KPI cơ bản
            long totalUsers = adminUserRepository.count();
            long totalOrders = adminOrderRepository.count();
            long totalProducts = adminProductRepository.count();
            
            // Tính toán conversion rate (placeholder)
            double conversionRate = totalOrders > 0 && totalUsers > 0 ? 
                (double) totalOrders / totalUsers * 100 : 0.0;
            
            // Tính average order value
            Double totalRevenueDouble = adminOrderRepository.calculateTotalRevenue();
            BigDecimal totalRevenue = totalRevenueDouble != null ? 
                BigDecimal.valueOf(totalRevenueDouble) : BigDecimal.ZERO;
            BigDecimal avgOrderValue = totalOrders > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
            
            kpis.put("conversionRate", conversionRate);
            kpis.put("averageOrderValue", avgOrderValue);
            kpis.put("totalCustomers", totalUsers);
            kpis.put("totalOrders", totalOrders);
            kpis.put("totalRevenue", totalRevenue);
            kpis.put("productCatalogSize", totalProducts);
            
            // TODO: Thêm các KPI khác khi có đủ dữ liệu
            kpis.put("customerRetentionRate", 0.0); // Placeholder
            kpis.put("averageSessionDuration", 0.0); // Placeholder
            
            log.info("Tính toán KPI thành công");
            return kpis;
            
        } catch (Exception e) {
            log.error("Lỗi khi tính toán KPI", e);
            return Map.of("error", "Không thể tính toán KPI");
        }
    }
}