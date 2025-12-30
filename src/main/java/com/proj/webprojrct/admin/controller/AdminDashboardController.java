package com.proj.webprojrct.admin.controller;

// Tạm thời comment out import chưa sử dụng
// import com.proj.webprojrct.admin.repository.AdminDashboardRepository;
import com.proj.webprojrct.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller cho dashboard tổng quan admin
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    // Sử dụng AdminDashboardService thay vì gọi trực tiếp repositories
    private final AdminDashboardService adminDashboardService;

    // Helper method để giảm code trùng lặp
    private <T> ResponseEntity<T> ok(T data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Lấy thống kê tổng quan cho Android App
     * GET /api/admin/dashboard/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> overview = adminDashboardService.getDashboardOverview();
        return ok(overview);
    }

    /**
     * Lấy thống kê tổng quan dashboard
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> overview = adminDashboardService.getDashboardOverview();
        return ok(overview);
    }

    /**
     * Lấy thống kê người dùng
     */
    @GetMapping("/stats/users")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = adminDashboardService.getDetailedStatistics();
        @SuppressWarnings("unchecked")
        Map<String, Object> userStats = (Map<String, Object>) stats.get("users");
        return ok(userStats);
    }

    /**
     * Lấy thống kê sản phẩm
     */
    @GetMapping("/stats/products")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        Map<String, Object> stats = adminDashboardService.getDetailedStatistics();
        @SuppressWarnings("unchecked")
        Map<String, Object> productStats = (Map<String, Object>) stats.get("products");
        return ok(productStats);
    }

    /**
     * Lấy thống kê đơn hàng
     */
    @GetMapping("/stats/orders")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        Map<String, Object> stats = adminDashboardService.getDetailedStatistics();
        @SuppressWarnings("unchecked")
        Map<String, Object> orderStats = (Map<String, Object>) stats.get("orders");
        return ok(orderStats);
    }

    /**
     * Lấy dữ liệu biểu đồ doanh thu
     */
    @GetMapping("/charts/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueChart(
        @RequestParam(defaultValue = "30") Integer days) {

    if (days < 1 || days > 365) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 400);
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", "Tham số 'days' phải nằm trong khoảng 1–365.");
        return ResponseEntity.badRequest().body(errorResponse);
    }
    Map<String, Object> chartData = adminDashboardService.getChartData("revenue", days);
    return ResponseEntity.ok(chartData);
    }


    /**
     * Lấy dữ liệu biểu đồ đơn hàng
     */
    @GetMapping("/charts/orders")
    public ResponseEntity<Map<String, Object>> getOrderChart(
        @RequestParam(defaultValue = "30") Integer days) {

    if (days < 1 || days > 365) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 400);
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", "Tham số 'days' phải nằm trong khoảng 1–365.");
        return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> chartData = adminDashboardService.getChartData("orders", days);
        return ResponseEntity.ok(chartData);
    }


    /**
     * Lấy hoạt động gần đây
     */
    @GetMapping("/recent-activities")
    public ResponseEntity<Map<String, Object>> getRecentActivities() {
        Map<String, Object> activities = adminDashboardService.getRecentActivities(10);
        return ok(activities);
    }

    /**
     * Lấy thông báo và cảnh báo
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAlerts() {
        Map<String, Object> alerts = adminDashboardService.getSystemAlerts();
        return ok(alerts);
    }

    /**
     * Lấy phân bố trạng thái đơn hàng
     */
    @GetMapping("/order-status")
    public ResponseEntity<Map<String, Object>> getOrderStatusDistribution() {
        Map<String, Object> distribution = adminDashboardService.getOrderStatusDistribution();
        return ok(distribution);
    }

    /**
     * Lấy danh sách sản phẩm bán chạy
     */
    @GetMapping("/top-products")
    public ResponseEntity<Map<String, Object>> getTopProducts(
        @RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> topProducts = adminDashboardService.getTopSellingProducts(limit);
        return ok(topProducts);
    }

    /**
     * Xuất báo cáo tổng hợp
     */
    @GetMapping("/export/summary")
    public ResponseEntity<Map<String, Object>> exportSummaryReport() {
        // Sử dụng tất cả dữ liệu từ dashboard service
        Map<String, Object> overview = adminDashboardService.getDashboardOverview();
        Map<String, Object> detailedStats = adminDashboardService.getDetailedStatistics();
        Map<String, Object> kpis = adminDashboardService.calculateKPIs();
        
        Map<String, Object> report = new HashMap<>();
        report.put("overview", overview);
        report.put("detailedStatistics", detailedStats);
        report.put("kpis", kpis);
        report.put("exportTime", java.time.LocalDateTime.now());
        
        return ok(report);
    }
}