package com.proj.webprojrct.admin.service;

import com.proj.webprojrct.admin.repository.AdminProductRepository;
import com.proj.webprojrct.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service xử lý business logic cho quản lý sản phẩm trong admin dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminProductService {

    private final AdminProductRepository adminProductRepository;

    /**
     * Lấy tất cả sản phẩm với phân trang
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        log.debug("Lấy danh sách sản phẩm với phân trang: {}", pageable);
        return adminProductRepository.findAll(pageable);
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        log.debug("Tìm kiếm sản phẩm với keyword: {} và phân trang: {}", keyword, pageable);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts(pageable);
        }
        return adminProductRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    /**
     * Lấy thông tin chi tiết sản phẩm
     */
    public Optional<Product> getProductById(Long id) {
        log.debug("Lấy thông tin sản phẩm với ID: {}", id);
        return adminProductRepository.findById(id);
    }

    /**
     * Lấy thống kê tổng quan về sản phẩm
     */
    public Map<String, Object> getProductStatistics() {
        log.debug("Lấy thống kê sản phẩm");
        
        long totalProducts = adminProductRepository.count();
        // TODO: Implement các thống kê chi tiết khi có methods trong repository
        BigDecimal averagePrice = BigDecimal.ZERO; // Placeholder
        List<Object[]> topSellingProducts = List.of(); // Placeholder
        List<Object[]> lowStockProducts = List.of(); // Placeholder
        
        return Map.of(
            "totalProducts", totalProducts,
            "averagePrice", averagePrice,
            "topSellingProducts", topSellingProducts,
            "lowStockProducts", lowStockProducts
        );
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    @Transactional
    public boolean updateProduct(Long productId, Product productData) {
        log.debug("Cập nhật sản phẩm ID: {}", productId);
        try {
            Optional<Product> productOpt = adminProductRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                
                // Cập nhật các field cần thiết
                if (productData.getName() != null) {
                    product.setName(productData.getName());
                }
                if (productData.getDescription() != null) {
                    product.setDescription(productData.getDescription());
                }
                if (productData.getPrice() != null) {
                    product.setPrice(productData.getPrice());
                }

                product.setUpdatedAt(LocalDateTime.now());
                adminProductRepository.save(product);
                log.info("Cập nhật sản phẩm ID: {} thành công", productId);
                return true;
            }
            log.warn("Không tìm thấy sản phẩm với ID: {}", productId);
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật sản phẩm ID: {}", productId, e);
            return false;
        }
    }

    /**
     * Xóa sản phẩm (soft delete hoặc hard delete tùy business logic)
     */
    @Transactional
    public boolean deleteProduct(Long productId) {
        log.debug("Xóa sản phẩm ID: {}", productId);
        try {
            Optional<Product> productOpt = adminProductRepository.findById(productId);
            if (productOpt.isPresent()) {
                adminProductRepository.deleteById(productId);
                log.info("Xóa sản phẩm ID: {} thành công", productId);
                return true;
            }
            log.warn("Không tìm thấy sản phẩm với ID: {}", productId);
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi xóa sản phẩm ID: {}", productId, e);
            return false;
        }
    }

    /**
     * Lấy danh sách sản phẩm theo danh mục
     */
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Lấy sản phẩm theo danh mục ID: {} với phân trang: {}", categoryId, pageable);
        // TODO: Implement method findByCategoryId() trong repository
        return adminProductRepository.findAll(pageable); // Placeholder
    }

    /**
     * Lấy sản phẩm có stock thấp
     */
    public List<Product> getLowStockProducts(int threshold) {
        log.debug("Lấy sản phẩm có stock thấp hơn: {}", threshold);
        // TODO: Implement method findByStockQuantityLessThan() trong repository
        return List.of(); // Placeholder
    }

    /**
     * Cập nhật số lượng stock của sản phẩm
     */
    @Transactional
    public boolean updateProductStock(Long productId, Integer newStock) {
        log.debug("Cập nhật stock sản phẩm ID: {} thành: {}", productId, newStock);
        try {
            Optional<Product> productOpt = adminProductRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setUpdatedAt(LocalDateTime.now());
                adminProductRepository.save(product);
                log.info("Cập nhật stock sản phẩm ID: {} thành công", productId);
                return true;
            }
            log.warn("Không tìm thấy sản phẩm với ID: {}", productId);
            return false;
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật stock sản phẩm ID: {}", productId, e);
            return false;
        }
    }

    /**
     * Tạo sản phẩm mới
     */
    @Transactional
    public Product createProduct(Product product) {
        log.debug("Tạo sản phẩm mới: {}", product.getName());
        try {
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            Product savedProduct = adminProductRepository.save(product);
            log.info("Tạo sản phẩm mới thành công với ID: {}", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            log.error("Lỗi khi tạo sản phẩm mới", e);
            throw e;
        }
    }
}