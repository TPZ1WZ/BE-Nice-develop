package com.proj.webprojrct.admin.repository;

import com.proj.webprojrct.order.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminOrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Tìm tất cả OrderItem theo orderId
    Page<OrderItem> findByOrder_Id(Long orderId, Pageable pageable);

    // Tìm tất cả OrderItem theo productId
    Page<OrderItem> findByProduct_Id(Long productId, Pageable pageable);

    // Thống kê sản phẩm bán chạy nhất theo số lượng
    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalQuantity, COUNT(oi) as orderCount " +
           "FROM OrderItem oi " +
           "GROUP BY oi.product.id " +
           "ORDER BY SUM(oi.quantity) DESC")
    Page<Object[]> findBestSellingProductsByQuantity(Pageable pageable);

    // Thống kê sản phẩm theo doanh thu (quantity * price)
    @Query("SELECT oi.product.id, SUM(oi.quantity * oi.productPrice) as totalRevenue, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi " +
           "GROUP BY oi.product.id " +
           "ORDER BY SUM(oi.quantity * oi.productPrice) DESC")
    Page<Object[]> findBestSellingProductsByRevenue(Pageable pageable);

    // Thống kê sản phẩm trong khoảng thời gian
    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalQuantity, SUM(oi.quantity * oi.productPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.product.id " +
           "ORDER BY SUM(oi.quantity * oi.productPrice) DESC")
    Page<Object[]> findProductStatsInPeriod(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    // Thống kê sản phẩm theo tháng
    @Query("SELECT oi.product.id, MONTH(o.createdAt) as month, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE YEAR(o.createdAt) = :year " +
           "GROUP BY oi.product.id, MONTH(o.createdAt) " +
           "ORDER BY MONTH(o.createdAt), SUM(oi.quantity) DESC")
    List<Object[]> findProductStatsByMonth(@Param("year") int year);

    // Tìm OrderItem có giá trị cao nhất
    @Query("SELECT oi FROM OrderItem oi ORDER BY (oi.quantity * oi.productPrice) DESC")
    Page<OrderItem> findHighestValueOrderItems(Pageable pageable);

    // Tính tổng doanh thu từ tất cả OrderItem
    @Query("SELECT SUM(oi.quantity * oi.productPrice) FROM OrderItem oi")
    Double calculateTotalRevenue();

    // Tính tổng số lượng sản phẩm đã bán
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi")
    Long calculateTotalQuantitySold();

    // Đếm số lượng OrderItem theo productId
    Long countByProduct_Id(Long productId);

    // Tìm tất cả OrderItem có quantity lớn hơn số lượng chỉ định
    @Query("SELECT oi FROM OrderItem oi WHERE oi.quantity > :minQuantity")
    Page<OrderItem> findByQuantityGreaterThan(@Param("minQuantity") Integer minQuantity, Pageable pageable);

    // Tìm OrderItem theo khoảng giá
    @Query("SELECT oi FROM OrderItem oi WHERE oi.productPrice BETWEEN :minPrice AND :maxPrice")
    Page<OrderItem> findByPriceBetween(@Param("minPrice") Double minPrice, 
                                      @Param("maxPrice") Double maxPrice, 
                                      Pageable pageable);

    // Tìm những sản phẩm chưa từng được bán (so với danh sách sản phẩm)
    @Query("SELECT p.id FROM Product p WHERE p.id NOT IN (SELECT DISTINCT oi.product.id FROM OrderItem oi)")
    List<Long> findNeverSoldProductIds();

    // Thống kê top sản phẩm theo doanh thu trong tuần
    @Query("SELECT oi.product.id, SUM(oi.quantity * oi.productPrice) as weeklyRevenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.createdAt >= :startOfWeek " +
           "GROUP BY oi.product.id " +
           "ORDER BY SUM(oi.quantity * oi.productPrice) DESC")
    Page<Object[]> findTopProductsThisWeek(@Param("startOfWeek") LocalDateTime startOfWeek, Pageable pageable);

    // Thống kê số lượng sản phẩm bán được hôm nay
    @Query("SELECT oi.product.id, SUM(oi.quantity) as todayQuantity " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay " +
           "GROUP BY oi.product.id " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findProductsSoldToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Tìm OrderItem theo orderId và productId
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.product.id = :productId")
    List<OrderItem> findByOrderIdAndProductId(@Param("orderId") Long orderId, @Param("productId") Long productId);

    // Tính giá trị trung bình của OrderItem
    @Query("SELECT AVG(oi.quantity * oi.productPrice) FROM OrderItem oi")
    Double calculateAverageOrderItemValue();

    // Tìm OrderItem có số lượng cao nhất
    @Query("SELECT oi FROM OrderItem oi ORDER BY oi.quantity DESC")
    Page<OrderItem> findHighestQuantityOrderItems(Pageable pageable);
}