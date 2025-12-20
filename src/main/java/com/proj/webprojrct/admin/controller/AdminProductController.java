package com.proj.webprojrct.admin.controller;

import com.proj.webprojrct.admin.dto.AdminProductDTO;
import com.proj.webprojrct.admin.dto.ProductStatsDTO;
import com.proj.webprojrct.admin.repository.AdminProductRepository;
import com.proj.webprojrct.cart.repository.CartItemRepository;
import com.proj.webprojrct.category.entity.Category;
import com.proj.webprojrct.category.repository.CategoryRepository;
import com.proj.webprojrct.favorite.repository.FavoriteRepository;
import com.proj.webprojrct.order.repository.OrderItemRepository;
import com.proj.webprojrct.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller quản lý sản phẩm trong admin dashboard
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductRepository adminProductRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final FavoriteRepository favoriteRepository;

    // Helper methods để giảm code trùng lặp
    private <T> ResponseEntity<T> ok(T data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Mapper: Product entity -> AdminProductDTO
     */
    private AdminProductDTO toDTO(Product product) {
        return AdminProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku("SKU-" + product.getId()) // Generate SKU from ID
                .category(product.getCategory() != null ? product.getCategory().getName() : "Uncategorized")
                .price(product.getPrice())
                .salePrice(null) // Product entity không có field này
                .stock(product.getStock())
                .status(product.isDelete() ? "inactive" : "active") // Map isDelete -> status
                .image(product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null) // Lấy ảnh đầu tiên
                .description(product.getDescription())
                .sizes(product.getSizes()) // Thêm sizes
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    /**
     * Lấy danh sách tất cả sản phẩm với search query (Android app cần format này)
     * GET /api/admin/products?search=nike
     * CHỈ LẤY SẢN PHẨM CHƯƠ XÓA (isDelete = false)
     */
    @GetMapping
    public ResponseEntity<List<AdminProductDTO>> getAllProducts(
            @RequestParam(required = false) String search) {
        
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            // Search by name - CHỈ LẤY SẢN PHẨM CHƯƠ XÓA
            Page<Product> productPage = adminProductRepository.findByNameContainingIgnoreCaseAndIsDeleteFalse(
                search.trim(), org.springframework.data.domain.PageRequest.of(0, 100));
            products = productPage.getContent();
        } else {
            // Get all products - CHỈ LẤY SẢN PHẨM CHƯƠ XÓA
            products = adminProductRepository.findByIsDeleteFalse();
        }
        
        // Convert to DTO
        List<AdminProductDTO> dtos = products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ok(dtos);
    }

    /**
     * Lấy thông tin chi tiết một sản phẩm
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminProductDTO> getProductById(@PathVariable Long id) {
        return adminProductRepository.findById(id)
                .map(this::toDTO)
                .map(this::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo sản phẩm mới
     */
    @PostMapping
    public ResponseEntity<AdminProductDTO> createProduct(@RequestBody AdminProductDTO dto) {
        // Convert DTO -> Entity
        Product product = new Product();
        product.setName(dto.getName());
        // SKU không có trong Product entity - bỏ qua
        
        // Xử lý Category
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findByNameIgnoreCase(dto.getCategory())
                    .orElseGet(() -> {
                        // Nếu chưa có category, tạo mới
                        Category newCat = new Category();
                        newCat.setName(dto.getCategory());
                        newCat.setIsDelete(false);
                        return categoryRepository.save(newCat);
                    });
            product.setCategory(category);
        }
        
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setDelete("inactive".equals(dto.getStatus())); // status -> isDelete
        
        // Set sizes
        if (dto.getSizes() != null && !dto.getSizes().isEmpty()) {
            product.setSizes(dto.getSizes());
        }
        
        // Note: images cần convert String -> List<String>
        if (dto.getImage() != null) {
            product.setImages(List.of(dto.getImage()));
        }
        
        Product savedProduct = adminProductRepository.save(product);
        return ok(toDTO(savedProduct));
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody AdminProductDTO dto) {
        
        return adminProductRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(dto.getName());
                    // SKU không có trong Product entity - bỏ qua
                    
                    // Xử lý Category
                    if (dto.getCategory() != null) {
                        Category category = categoryRepository.findByNameIgnoreCase(dto.getCategory())
                                .orElseGet(() -> {
                                    Category newCat = new Category();
                                    newCat.setName(dto.getCategory());
                                    newCat.setIsDelete(false);
                                    return categoryRepository.save(newCat);
                                });
                        existingProduct.setCategory(category);
                    }
                    
                    existingProduct.setDescription(dto.getDescription());
                    existingProduct.setPrice(dto.getPrice());
                    existingProduct.setStock(dto.getStock());
                    existingProduct.setDelete("inactive".equals(dto.getStatus()));
                    
                    // Update sizes
                    if (dto.getSizes() != null && !dto.getSizes().isEmpty()) {
                        existingProduct.setSizes(dto.getSizes());
                    }
                    
                    if (dto.getImage() != null) {
                        existingProduct.setImages(List.of(dto.getImage()));
                    }

                    Product updatedProduct = adminProductRepository.save(existingProduct);
                    return ok(toDTO(updatedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Xóa sản phẩm thông minh:
     * - Xóa khỏi giỏ hàng (cart_items)
     * - Xóa khỏi danh sách yêu thích (favorites)
     * - Soft delete sản phẩm (set isDelete = true) để giữ lịch sử đơn hàng
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<java.util.Map<String, Object>> deleteProduct(@PathVariable Long id) {
        return adminProductRepository.findById(id)
                .map(product -> {
                    try {
                        long cartItemsRemoved = 0;
                        long favoritesRemoved = 0;
                        boolean hasOrders = false;
                        
                        // 1. Kiểm tra xem có trong đơn hàng không (dùng query hiệu quả)
                        hasOrders = orderItemRepository.existsByProductId(id);
                        log.info("Sản phẩm ID {} có trong đơn hàng: {}", id, hasOrders);
                        
                        // 2. Đếm và xóa khỏi giỏ hàng (dùng query hiệu quả)
                        cartItemsRemoved = cartItemRepository.countByProductId(id);
                        if (cartItemsRemoved > 0) {
                            cartItemRepository.deleteByProductId(id);
                            log.info("Đã xóa {} mục trong giỏ hàng cho sản phẩm ID: {}", cartItemsRemoved, id);
                        }
                        
                        // 3. Đếm và xóa khỏi danh sách yêu thích (dùng query hiệu quả)
                        favoritesRemoved = favoriteRepository.countByProductId(id);
                        if (favoritesRemoved > 0) {
                            favoriteRepository.deleteByProductId(id);
                            log.info("Đã xóa {} mục yêu thích cho sản phẩm ID: {}", favoritesRemoved, id);
                        }
                        
                        // 4. Soft delete hoặc hard delete
                        if (hasOrders) {
                            // Nếu có trong đơn hàng, chỉ soft delete
                            product.setDelete(true);
                            adminProductRepository.save(product);
                            log.info("Soft delete sản phẩm ID: {} (có trong đơn hàng)", id);
                        } else {
                            // Nếu không có trong đơn hàng, có thể hard delete
                            adminProductRepository.delete(product);
                            log.info("Hard delete sản phẩm ID: {}", id);
                        }
                        
                        // Trả về thông tin chi tiết
                        java.util.Map<String, Object> successResponse = java.util.Map.of(
                            "success", true,
                            "message", "Đã xóa sản phẩm thành công",
                            "details", java.util.Map.of(
                                "cartItemsRemoved", (int)cartItemsRemoved,
                                "favoritesRemoved", (int)favoritesRemoved,
                                "softDelete", hasOrders
                            )
                        );
                        return ResponseEntity.ok(successResponse);
                    } catch (Exception e) {
                        log.error("Lỗi khi xóa sản phẩm ID: {}", id, e);
                        java.util.Map<String, Object> errorResponse = java.util.Map.of(
                            "success", false,
                            "message", "Lỗi khi xóa sản phẩm: " + e.getMessage()
                        );
                        return ResponseEntity.status(500).body(errorResponse);
                    }
                })
                .orElseGet(() -> {
                    java.util.Map<String, Object> notFoundResponse = java.util.Map.of(
                        "success", false,
                        "message", "Không tìm thấy sản phẩm"
                    );
                    return ResponseEntity.status(404).body(notFoundResponse);
                });
    }

    /**
     * Thống kê sản phẩm (Android app cần endpoint này)
     * GET /api/admin/products/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ProductStatsDTO> getProductStats() {
        // CHỈ ĐẾM SẢN PHẨM CHƯƠ XÓA
        List<Product> allProducts = adminProductRepository.findByIsDeleteFalse();
        
        int total = allProducts.size();
        int outOfStock = (int) allProducts.stream()
                .filter(p -> p.getStock() == null || p.getStock() == 0)
                .count();
        int lowStock = (int) allProducts.stream()
                .filter(p -> p.getStock() != null && p.getStock() > 0 && p.getStock() < 10)
                .count();
        
        ProductStatsDTO stats = ProductStatsDTO.builder()
                .total(total)
                .outOfStock(outOfStock)
                .lowStock(lowStock)
                .build();
        
        return ok(stats);
    }
}