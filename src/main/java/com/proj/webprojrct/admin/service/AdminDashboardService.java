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
 * Service tổng hợp cho admin dashboard - xử lý logic phức tạp kết hợp nhiều
 * domain
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
            // Thống kê doanh thu (đã dùng logic mới trong repository: finalAmount + status
            // IN)
            Double totalRevenueDouble = adminOrderRepository.calculateTotalRevenue();
            BigDecimal totalRevenue = totalRevenueDouble != null ? BigDecimal.valueOf(totalRevenueDouble)
                    : BigDecimal.ZERO;
            overview.put("totalRevenue", totalRevenue);

            // --- Tính toán Growth (Tăng trưởng so với 30 ngày trước) ---
            LocalDate now = LocalDate.now();
            LocalDateTime endDate = now.atTime(23, 59, 59);
            LocalDateTime startDate = now.minusDays(30).atStartOfDay();
            
            LocalDateTime prevEndDate = now.minusDays(30).atTime(23, 59, 59);
            LocalDateTime prevStartDate = now.minusDays(60).atStartOfDay();

            // 1. Revenue Growth (30 ngày gần nhất vs 30 ngày trước đó)
            Double currentPeriodRevenue = adminOrderRepository.calculateRevenueInPeriod(startDate, endDate);
            Double prevPeriodRevenue = adminOrderRepository.calculateRevenueInPeriod(prevStartDate, prevEndDate);

            double revenueGrowth = 0.0;
            if (prevPeriodRevenue != null && prevPeriodRevenue > 0) {
                double current = currentPeriodRevenue != null ? currentPeriodRevenue : 0.0;
                revenueGrowth = ((current - prevPeriodRevenue) / prevPeriodRevenue) * 100;
            } else if (currentPeriodRevenue != null && currentPeriodRevenue > 0) {
                revenueGrowth = 100.0; // Từ 0 lên có doanh thu -> 100%
            }
            overview.put("revenueGrowth", revenueGrowth);
            overview.put("monthlyRevenue", BigDecimal.valueOf(currentPeriodRevenue != null ? currentPeriodRevenue : 0.0));

            // 2. Order Growth (30 ngày gần nhất vs 30 ngày trước đó)
            Long currentPeriodOrders = adminOrderRepository.countOrdersInPeriod(startDate, endDate);
            Long prevPeriodOrders = adminOrderRepository.countOrdersInPeriod(prevStartDate, prevEndDate);

            double ordersGrowth = 0.0;
            if (prevPeriodOrders != null && prevPeriodOrders > 0) {
                long current = currentPeriodOrders != null ? currentPeriodOrders : 0;
                ordersGrowth = ((double) (current - prevPeriodOrders) / prevPeriodOrders) * 100;
            } else if (currentPeriodOrders != null && currentPeriodOrders > 0) {
                ordersGrowth = 100.0;
            }
            overview.put("ordersGrowth", ordersGrowth);

            // 3. User Growth (Placeholder logic - cần method trong repository nếu muốn
            // chính xác theo tháng)
            // Tạm thời để 0 hoặc random cho user experience demo (ở đây để 0 an toàn)
            overview.put("usersGrowth", 0.0);

            // 4. Products Out of Stock
            // Cần query countByStockQuantityLessThan(1)
            // Tạm thời giả định method đó chưa có, dùng lại logic cũ hoặc thêm method vào
            // repo sau
            // overview.put("productsOutOfStock", adminProductRepository.countOutOfStock());
            // Nếu repo chưa có, tạm hardcode hoặc dùng countByStatus('out_of_stock') nếu có
            overview.put("productsOutOfStock", 0); // Placeholder an toàn

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
            userStats.put("activeUsers", adminUserRepository.count()); // Placeholder cho đến khi có method
                                                                       // countActiveUsers()
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
            BigDecimal totalRevenue = totalRevenueDouble != null ? BigDecimal.valueOf(totalRevenueDouble)
                    : BigDecimal.ZERO;
            orderStats.put("totalRevenue", totalRevenue);
            // TODO: Implement countOrdersByStatus() method
            orderStats.put("byStatus", Map.of()); // Placeholder
            stats.put("orders", orderStats);

            // Thống kê đánh giá
            Map<String, Object> reviewStats = new HashMap<>();
            reviewStats.put("total", 0);
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
            LocalDateTime startDate;
            
            // Tính startDate dựa trên kỳ thống kê
            if (days == 0) {
                // Tháng này: từ ngày 1 của tháng đến hiện tại
                startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            } else if (days == 1) {
                // Hôm nay: từ 00:00:00 hôm nay đến hiện tại
                startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            } else {
                // 7 ngày hoặc 30 ngày: từ 00:00:00 của N ngày trước đến hiện tại
                startDate = LocalDateTime.now().minusDays(days - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            }
            
            log.debug("Querying revenue from {} to {}", startDate, endDate);

            switch (chartType.toLowerCase()) {
                case "revenue":
                    List<Object[]> revenueData = adminOrderRepository.findRevenueByDateRange(startDate, endDate);
                    log.debug("Revenue data rows returned: {}", revenueData.size());
                    
                    // Tạo Map để tra cứu nhanh doanh thu theo ngày
                    Map<LocalDate, Double> revenueMap = new HashMap<>();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
                    
                    for (Object[] row : revenueData) {
                        log.debug("Processing row: date type={}, revenue type={}", 
                                  row[0] != null ? row[0].getClass().getName() : "null",
                                  row[1] != null ? row[1].getClass().getName() : "null");
                        
                        // Skip null rows
                        if (row[0] == null) {
                            log.warn("Skipping row with null date");
                            continue;
                        }
                        
                        // Convert date to LocalDate (handle both java.sql.Date and LocalDate)
                        LocalDate date;
                        if (row[0] instanceof LocalDate) {
                            date = (LocalDate) row[0];
                        } else if (row[0] instanceof java.sql.Date) {
                            date = ((java.sql.Date) row[0]).toLocalDate();
                        } else {
                            log.warn("Unexpected date type: {}", row[0].getClass().getName());
                            continue;
                        }
                        
                        Double revenue = (Double) row[1];
                        revenueMap.put(date, revenue != null ? revenue : 0.0);
                        
                        log.debug("Revenue on {}: {}", date, revenue);
                    }
                    
                    // Tạo danh sách đầy đủ các ngày trong khoảng thời gian
                    List<String> revenueLabels = new ArrayList<>();
                    List<Double> revenueValues = new ArrayList<>();
                    
                    LocalDate currentDate = startDate.toLocalDate();
                    LocalDate lastDate = endDate.toLocalDate();
                    
                    while (!currentDate.isAfter(lastDate)) {
                        String label = currentDate.format(formatter);
                        Double revenue = revenueMap.getOrDefault(currentDate, 0.0);
                        
                        revenueLabels.add(label);
                        revenueValues.add(revenue);
                        
                        log.debug("Added to chart: {} = {}", label, revenue);
                        currentDate = currentDate.plusDays(1);
                    }

                    chartData.put("labels", revenueLabels);
                    chartData.put("data", revenueValues);
                    chartData.put("title", "Doanh thu " + days + " ngày qua");
                    
                    // Tính tổng doanh thu kỳ hiện tại
                    double currentPeriodTotal = revenueValues.stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();
                    
                    // Lấy dữ liệu kỳ trước để so sánh
                    LocalDateTime previousEndDate = startDate.minusSeconds(1);
                    LocalDateTime previousStartDate;
                    
                    if (days == 0) {
                        // Tháng trước
                        previousStartDate = startDate.minusMonths(1);
                    } else if (days == 1) {
                        // Hôm qua
                        previousStartDate = startDate.minusDays(1);
                    } else {
                        // N ngày trước đó
                        previousStartDate = startDate.minusDays(days);
                    }
                    
                    log.debug("Querying previous period revenue from {} to {}", previousStartDate, previousEndDate);
                    
                    // Query doanh thu kỳ trước
                    List<Object[]> previousRevenueData = adminOrderRepository.findRevenueByDateRange(previousStartDate, previousEndDate);
                    
                    double previousPeriodTotal = 0.0;
                    for (Object[] row : previousRevenueData) {
                        if (row[1] != null) {
                            previousPeriodTotal += (Double) row[1];
                        }
                    }
                    
                    // Tính phần trăm thay đổi
                    double changePercent = 0.0;
                    if (previousPeriodTotal > 0) {
                        changePercent = ((currentPeriodTotal - previousPeriodTotal) / previousPeriodTotal) * 100;
                    } else if (currentPeriodTotal > 0) {
                        changePercent = 100.0;
                    }
                    
                    chartData.put("previousPeriodTotal", previousPeriodTotal);
                    chartData.put("changePercent", changePercent);
                    
                    log.info("Revenue chart prepared: {} days, current={}, previous={}, change={}%", 
                             days, currentPeriodTotal, previousPeriodTotal, String.format("%.1f", changePercent));
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
            log.error("Lỗi khi lấy dữ liệu biểu đồ {}: {}", chartType, e.getMessage(), e);
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
                if (count >= limit)
                    break;

                Map<String, Object> product = new HashMap<>();
                product.put("id", row[0]);
                product.put("name", row[1]);
                product.put("soldQuantity", ((Number) row[2]).intValue());

                // Handle images
                List<String> images = (List<String>) row[3];
                if (images != null && !images.isEmpty()) {
                    product.put("imageUrl", images.get(0));
                } else {
                    product.put("imageUrl", "");
                }

                // Handle sku (using slug for now)
                String slug = (String) row[4];
                product.put("sku", slug != null ? slug : "");

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
            double conversionRate = totalOrders > 0 && totalUsers > 0 ? (double) totalOrders / totalUsers * 100 : 0.0;

            // Tính average order value
            Double totalRevenueDouble = adminOrderRepository.calculateTotalRevenue();
            BigDecimal totalRevenue = totalRevenueDouble != null ? BigDecimal.valueOf(totalRevenueDouble)
                    : BigDecimal.ZERO;
            BigDecimal avgOrderValue = totalOrders > 0
                    ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

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