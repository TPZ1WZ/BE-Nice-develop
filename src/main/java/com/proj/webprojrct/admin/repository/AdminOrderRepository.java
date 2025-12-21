package com.proj.webprojrct.admin.repository;

import com.proj.webprojrct.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminOrderRepository extends JpaRepository<Order, Long> {

       // Tìm tất cả đơn hàng với phân trang
       Page<Order> findAll(Pageable pageable);

       // Tìm đơn hàng theo trạng thái
       Page<Order> findByStatusIgnoreCase(String status, Pageable pageable);

       // Tìm đơn hàng theo phương thức thanh toán
       Page<Order> findByPaymentMethodIgnoreCase(String paymentMethod, Pageable pageable);

       // Tìm đơn hàng theo người dùng
       Page<Order> findByUser_Id(Long userId, Pageable pageable);

       // Tìm đơn hàng theo khoảng thời gian
       @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
       Page<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     Pageable pageable);

       // Tìm đơn hàng theo khoảng tổng tiền
       @Query("SELECT o FROM Order o WHERE o.totalAmount BETWEEN :minAmount AND :maxAmount")
       Page<Order> findByTotalAmountBetween(@Param("minAmount") Double minAmount,
                     @Param("maxAmount") Double maxAmount,
                     Pageable pageable);

       /**
        * Tìm kiếm đơn hàng với nhiều bộ lọc kết hợp
        */
       @Query("SELECT o FROM Order o WHERE " +
                     "(:userId IS NULL OR o.user.id = :userId) AND " +
                     "(:status IS NULL OR LOWER(o.status) = LOWER(:status)) AND " +
                     "(:paymentMethod IS NULL OR LOWER(o.paymentMethod) = LOWER(:paymentMethod)) AND " +
                     "(:minAmount IS NULL OR o.totalAmount >= :minAmount) AND " +
                     "(:maxAmount IS NULL OR o.totalAmount <= :maxAmount) AND " +
                     "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
                     "(:endDate IS NULL OR o.createdAt <= :endDate) AND " +
                     "(:shippingAddress IS NULL OR LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :shippingAddress, '%')))")
       Page<Order> findWithFilters(@Param("userId") Long userId,
                     @Param("status") String status,
                     @Param("paymentMethod") String paymentMethod,
                     @Param("minAmount") Double minAmount,
                     @Param("maxAmount") Double maxAmount,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     @Param("shippingAddress") String shippingAddress,
                     Pageable pageable);

       // Tìm đơn hàng mới nhất
       @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
       Page<Order> findLatestOrders(Pageable pageable);

       // Tìm đơn hàng có giá trị cao nhất
       @Query("SELECT o FROM Order o ORDER BY o.totalAmount DESC")
       Page<Order> findHighestValueOrders(Pageable pageable);

       // Thống kê số lượng đơn hàng theo trạng thái
       @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
       List<Object[]> countOrdersByStatus();

       // Thống kê số lượng đơn hàng theo phương thức thanh toán
       @Query("SELECT o.paymentMethod, COUNT(o) FROM Order o GROUP BY o.paymentMethod")
       List<Object[]> countOrdersByPaymentMethod();

       // Thống kê doanh thu theo tháng trong năm
       @Query("SELECT MONTH(o.createdAt), SUM(o.totalAmount) FROM Order o " +
                     "WHERE YEAR(o.createdAt) = :year AND LOWER(o.status) = 'completed' " +
                     "GROUP BY MONTH(o.createdAt) " +
                     "ORDER BY MONTH(o.createdAt)")
       List<Object[]> calculateMonthlyRevenue(@Param("year") int year);

       // Thống kê số lượng đơn hàng theo tháng trong năm
       @Query("SELECT MONTH(o.createdAt), COUNT(o) FROM Order o " +
                     "WHERE YEAR(o.createdAt) = :year " +
                     "GROUP BY MONTH(o.createdAt) " +
                     "ORDER BY MONTH(o.createdAt)")
       List<Object[]> countOrdersByMonth(@Param("year") int year);

       // Thống kê doanh thu theo ngày trong tháng
       @Query("SELECT DAY(o.createdAt), SUM(o.totalAmount) FROM Order o " +
                     "WHERE YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month AND LOWER(o.status) = 'completed' "
                     +
                     "GROUP BY DAY(o.createdAt) " +
                     "ORDER BY DAY(o.createdAt)")
       List<Object[]> calculateDailyRevenueInMonth(@Param("year") int year, @Param("month") int month);

       // Tính tổng doanh thu từ đơn hàng (đã xác nhận, đang giao, hoàn thành) - sử
       // dụng finalAmount
       @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE LOWER(o.status) IN ('completed', 'shipping', 'confirmed')")
       Double calculateTotalRevenue();

       // Tính tổng doanh thu trong khoảng thời gian
       @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE " +
                     "LOWER(o.status) IN ('completed', 'shipping', 'confirmed') AND " +
                     "o.createdAt BETWEEN :startDate AND :endDate")
       Double calculateRevenueInPeriod(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       // Tính tổng doanh thu theo tháng cụ thể (cho tính growth)
       @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE " +
                     "YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month AND " +
                     "LOWER(o.status) IN ('completed', 'shipping', 'confirmed')")
       Double calculateMonthlyRevenueValue(@Param("year") int year, @Param("month") int month);

       // Tính giá trị đơn hàng trung bình
       @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE LOWER(o.status) = 'completed'")
       Double calculateAverageOrderValue();

       // Đếm số đơn hàng theo trạng thái (case sensitive vì status đã là lowercase)
       @Query("SELECT COUNT(o) FROM Order o WHERE LOWER(o.status) = LOWER(:status)")
       Long countByStatus(@Param("status") String status);

       // Đếm số đơn hàng theo trạng thái cụ thể (để backward compatibility)
       Long countByStatusIgnoreCase(String status);

       // Đếm tổng số đơn hàng hôm nay
       @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay")
       Long countTodayOrders(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

       // Đếm tổng số đơn hàng trong tuần
       @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfWeek")
       Long countWeeklyOrders(@Param("startOfWeek") LocalDateTime startOfWeek);

       // Đếm tổng số đơn hàng trong tháng
       @Query("SELECT COUNT(o) FROM Order o WHERE YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
       Long countMonthlyOrders(@Param("year") int year, @Param("month") int month);

       // Tìm top khách hàng theo tổng giá trị đơn hàng
       @Query("SELECT o.user.id, SUM(o.totalAmount), COUNT(o) FROM Order o " +
                     "WHERE LOWER(o.status) = 'completed' " +
                     "GROUP BY o.user.id " +
                     "ORDER BY SUM(o.totalAmount) DESC")
       Page<Object[]> findTopCustomersByValue(Pageable pageable);

       // Tìm top khách hàng theo số lượng đơn hàng
       @Query("SELECT o.user.id, COUNT(o), SUM(o.totalAmount) FROM Order o " +
                     "WHERE LOWER(o.status) = 'completed' " +
                     "GROUP BY o.user.id " +
                     "ORDER BY COUNT(o) DESC")
       Page<Object[]> findTopCustomersByOrderCount(Pageable pageable);

       // Tìm đơn hàng cần xử lý (pending, confirmed)
       @Query("SELECT o FROM Order o WHERE LOWER(o.status) IN ('pending', 'confirmed') ORDER BY o.createdAt ASC")
       Page<Order> findOrdersNeedingProcessing(Pageable pageable);

       // Tìm đơn hàng quá hạn xử lý (pending > 24h)
       @Query("SELECT o FROM Order o WHERE LOWER(o.status) = 'pending' AND o.createdAt < :cutoffTime")
       Page<Order> findOverdueOrders(@Param("cutoffTime") LocalDateTime cutoffTime, Pageable pageable);

       // Tìm đơn hàng theo ID để quản lý
       Optional<Order> findById(Long id);

       // Cập nhật trạng thái đơn hàng
       @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
       void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") String status);

       // Tìm tất cả trạng thái đơn hàng duy nhất
       @Query("SELECT DISTINCT o.status FROM Order o ORDER BY o.status")
       List<String> findAllStatuses();

       // Tìm tất cả phương thức thanh toán duy nhất
       @Query("SELECT DISTINCT o.paymentMethod FROM Order o ORDER BY o.paymentMethod")
       List<String> findAllPaymentMethods();

       /**
        * Lấy doanh thu theo ngày trong khoảng thời gian
        * Trả về: [Date (YYYY-MM-DD), Total Revenue]
        */
       @Query("SELECT CAST(o.createdAt AS date), SUM(o.totalAmount) " +
                     "FROM Order o " +
                     "WHERE o.createdAt >= :startDate AND o.createdAt < :endDate " +
                     "AND LOWER(o.status) IN ('completed', 'shipping') " +
                     "GROUP BY CAST(o.createdAt AS date) " +
                     "ORDER BY CAST(o.createdAt AS date) ASC")
       List<Object[]> findRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       /**
        * Lấy số lượng đơn hàng theo ngày trong khoảng thời gian
        * Trả về: [Date (YYYY-MM-DD), Order Count]
        */
       @Query("SELECT CAST(o.createdAt AS date), COUNT(o) " +
                     "FROM Order o " +
                     "WHERE o.createdAt >= :startDate AND o.createdAt < :endDate " +
                     "GROUP BY CAST(o.createdAt AS date) " +
                     "ORDER BY CAST(o.createdAt AS date) ASC")
       List<Object[]> findOrderCountByDateRange(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);
}