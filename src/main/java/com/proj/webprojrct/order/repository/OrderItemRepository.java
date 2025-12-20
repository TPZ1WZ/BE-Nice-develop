package com.proj.webprojrct.order.repository;

import com.proj.webprojrct.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Lấy top sản phẩm bán chạy (theo số lượng đã bán)
     * Trả về: [Product ID, Product Name, Total Quantity Sold]
     */
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status IN ('completed', 'shipping') " +
           "GROUP BY oi.product.id, oi.product.name " +
           "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts();
    
    // Kiểm tra sản phẩm có trong đơn hàng không
    boolean existsByProductId(Long productId);
}
