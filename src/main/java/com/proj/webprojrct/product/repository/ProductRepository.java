package com.proj.webprojrct.product.repository;

import com.proj.webprojrct.category.entity.Category;
import com.proj.webprojrct.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByIsDeleteFalseAndCategory_IsDeleteFalse(Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndIsDeleteFalseAndCategory_IsDeleteFalse(String text, Pageable pageable);
    Page<Product> findByCategoryAndIsDeleteIsFalse(Category category, Pageable pageable);
    Page<Product> findByCategoryAndNameContainingIgnoreCaseAndIsDeleteFalse(Category category, String text, Pageable pageable);
    // ➕ Price filtering
    Page<Product> findByPriceBetweenAndIsDeleteFalseAndCategory_IsDeleteFalse(double minPrice, double maxPrice, Pageable pageable);
    Page<Product> findByCategoryAndPriceBetweenAndIsDeleteFalse(Category category, double minPrice, double maxPrice, Pageable pageable);
    Page<Product> findByCategoryAndNameContainingIgnoreCaseAndPriceBetweenAndIsDeleteFalse(Category category, String text, double minPrice, double maxPrice, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndPriceBetweenAndIsDeleteFalseAndCategory_IsDeleteFalse(String text, double minPrice, double maxPrice, Pageable pageable);
    Page<Product> findByCategoryIdInAndIsDeleteFalse(List<Long> categoryIds, Pageable pageable);
    Page<Product> findByCategoryIdInAndNameContainingIgnoreCaseAndIsDeleteFalse(List<Long> categoryIds, String text, Pageable pageable);
    Page<Product> findByCategoryIdInAndPriceBetweenAndIsDeleteFalse(List<Long> categoryIds, Double min, Double max, Pageable pageable);
    Page<Product> findByCategoryIdInAndNameContainingIgnoreCaseAndPriceBetweenAndIsDeleteFalse(List<Long> categoryIds, String text, Double min, Double max, Pageable pageable);

    // Search by name
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Search by price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    
    // Search by stock availability
    List<Product> findByIsDeleteAndCategory_IsDeleteFalse(Boolean isDelete);
    
    // Combined search query
    @Query("SELECT p FROM Product p WHERE p.isDelete = false " +
           "AND (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> searchProducts(@Param("name") String name, 
                                @Param("minPrice") Double minPrice, 
                                @Param("maxPrice") Double maxPrice);

    // Simple method to get all non-deleted products with pagination
    Page<Product> findByIsDelete(Boolean isDelete, Pageable pageable);
    
    // Advanced filter query with pagination
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.isDelete = false " +
           "AND (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds)")
    Page<Product> findWithFilters(
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    // Advanced filter query without pagination
    @Query("SELECT p FROM Product p WHERE p.isDelete = false " +
           "AND (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    List<Product> findWithFilters(
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryId") Long categoryId);

    // Filter by multiple categories
    @Query("SELECT p FROM Product p WHERE p.isDelete = false " +
           "AND (:name IS NULL OR p.name LIKE :name) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:categoryIds IS NULL OR p.category.id IN :categoryIds)")
    Page<Product> findWithFiltersAndCategories(
            @Param("name") String name,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryIds") List<Long> categoryIds,
            Pageable pageable);

    // Featured products
    Page<Product> findByIsDeleteOrderByCreatedAtDesc(Boolean isDelete, Pageable pageable);
    
    // Search suggestions
    List<Product> findByNameContainingIgnoreCaseAndIsDelete(String name, Boolean isDelete, Pageable pageable);
}
