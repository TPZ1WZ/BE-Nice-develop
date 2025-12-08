package com.proj.webprojrct.admin.repository;

import com.proj.webprojrct.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminProductRepository extends JpaRepository<Product, Long> {

    // Tìm tất cả sản phẩm với phân trang
    Page<Product> findAll(Pageable pageable);

    // Tìm kiếm sản phẩm theo tên
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Tìm kiếm sản phẩm theo mô tả
    Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    // Lọc sản phẩm theo danh mục (removed - category field not exist in entity)
    // Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    // Lọc sản phẩm theo khoảng giá
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceBetween(@Param("minPrice") Double minPrice, 
                                    @Param("maxPrice") Double maxPrice, 
                                    Pageable pageable);

    // Tìm sản phẩm bán chạy nhất (removed - soldCount field not exist in entity)  
    // @Query("SELECT p FROM Product p ORDER BY p.soldCount DESC")
    // Page<Product> findBestSellingProducts(Pageable pageable);

    // Tìm sản phẩm mới nhất
    @Query("SELECT p FROM Product p ORDER BY p.id DESC")
    Page<Product> findLatestProducts(Pageable pageable);

    /**
     * Tìm kiếm sản phẩm với nhiều bộ lọc kết hợp
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR " +
           " LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findWithFilters(@Param("keyword") String keyword,
                                 @Param("minPrice") Double minPrice,
                                 @Param("maxPrice") Double maxPrice,
                                 Pageable pageable);

    // Tìm kiếm sản phẩm theo tags (removed - tags field not exist in entity)
    // @Query("SELECT p FROM Product p JOIN p.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :tag, '%'))")
    // Page<Product> findByTagsContainingIgnoreCase(@Param("tag") String tag, Pageable pageable);

    // Tìm kiếm sản phẩm theo sizes (removed - sizes field not exist in entity)
    // @Query("SELECT p FROM Product p JOIN p.sizes s WHERE LOWER(s) = LOWER(:size)")
    // Page<Product> findBySizesContainingIgnoreCase(@Param("size") String size, Pageable pageable);

    // Tìm kiếm sản phẩm theo colors (removed - colors field not exist in entity)
    // @Query("SELECT p FROM Product p JOIN p.colors c WHERE LOWER(c) = LOWER(:color)")
    // Page<Product> findByColorsContainingIgnoreCase(@Param("color") String color, Pageable pageable);

    // Thống kê số lượng sản phẩm theo danh mục (removed - category field not exist in entity)
    // @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category ORDER BY COUNT(p) DESC")
    // List<Object[]> countProductsByCategory();

    // Thống kê tổng doanh thu theo sản phẩm (removed - soldCount field not exist in entity)
    // @Query("SELECT p.id, p.name, p.price, p.soldCount, (p.price * p.soldCount) as revenue " +
    //        "FROM Product p " +
    //        "ORDER BY (p.price * p.soldCount) DESC")
    // Page<Object[]> findProductRevenueStats(Pageable pageable);

    // Tìm sản phẩm có giá cao nhất và thấp nhất
    @Query("SELECT MAX(p.price), MIN(p.price) FROM Product p")
    List<Object[]> findPriceRange();

    // Tìm sản phẩm theo khoảng số lượng đã bán (removed - soldCount field not exist in entity)
    // @Query("SELECT p FROM Product p WHERE p.soldCount BETWEEN :minSold AND :maxSold")
    // Page<Product> findBySoldCountBetween(@Param("minSold") Integer minSold, 
    //                                     @Param("maxSold") Integer maxSold, 
    //                                     Pageable pageable);

    // Đếm tổng số sản phẩm (đã có sẵn từ JpaRepository)

    // Tính tổng doanh thu từ tất cả sản phẩm (removed - soldCount field not exist in entity)
    // @Query("SELECT SUM(p.price * p.soldCount) FROM Product p")
    // Double calculateTotalRevenue();

    // Tính trung bình giá sản phẩm
    @Query("SELECT AVG(p.price) FROM Product p")
    Double calculateAveragePrice();

    // Tìm top sản phẩm theo doanh thu (removed - soldCount field not exist in entity)
    // @Query("SELECT p FROM Product p ORDER BY (p.price * p.soldCount) DESC")
    // Page<Product> findTopRevenueProducts(Pageable pageable);

    // Tìm sản phẩm ít bán nhất (removed - soldCount field not exist in entity)
    // @Query("SELECT p FROM Product p ORDER BY p.soldCount ASC")
    // Page<Product> findLeastSoldProducts(Pageable pageable);

    // Tìm sản phẩm theo ID để quản lý
    Optional<Product> findById(Long id);

    // Lấy top sản phẩm bán chạy nhất (dựa trên order_item)
    @Query("SELECT p.id, p.name, SUM(oi.quantity) " +
           "FROM Product p " +
           "JOIN OrderItem oi ON oi.product.id = p.id " +
           "JOIN Order o ON oi.order.id = o.id " +
           "WHERE o.status IN ('completed', 'shipping') " +
           "GROUP BY p.id, p.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProductsRaw(Pageable pageable);

    // Method wrapper để trả về dữ liệu đã format
    default List<java.util.Map<String, Object>> findTopSellingProducts() {
        var rawData = findTopSellingProductsRaw(org.springframework.data.domain.PageRequest.of(0, 5));
        return rawData.stream()
            .map(row -> {
                var map = new java.util.HashMap<String, Object>();
                map.put("id", row[0]);
                map.put("name", row[1]);
                map.put("soldQuantity", row[2] != null ? ((Number) row[2]).intValue() : 0);
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    // Tìm tất cả danh mục duy nhất (removed - category field not exist in entity)
    // @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    // List<String> findAllCategories();

    // Tìm tất cả tags duy nhất (removed - tags field not exist in entity)
    // @Query("SELECT DISTINCT t FROM Product p JOIN p.tags t ORDER BY t")
    // List<String> findAllTags();

    // Tìm tất cả sizes duy nhất (removed - sizes field not exist in entity)
    // @Query("SELECT DISTINCT s FROM Product p JOIN p.sizes s ORDER BY s")
    // List<String> findAllSizes();

    // Tìm tất cả colors duy nhất (removed - colors field not exist in entity)
    // @Query("SELECT DISTINCT c FROM Product p JOIN p.colors c ORDER BY c")
    // List<String> findAllColors();

    // Cập nhật số lượng đã bán của sản phẩm (removed - soldCount field not exist in entity)
    // @Query("UPDATE Product p SET p.soldCount = :soldCount WHERE p.id = :productId")
    // void updateSoldCount(@Param("productId") Long productId, @Param("soldCount") Integer soldCount);
}