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
                .image(product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null) // Lấy ảnh đầu tiên (backward compatibility)
                .images(product.getImages()) // Return full images list
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
     * LẤY TẤT CẢ SẢN PHẨM (bao gồm cả sản phẩm đã ẩn) để admin có thể quản lý
     */
    @GetMapping
    public ResponseEntity<List<AdminProductDTO>> getAllProducts(
            @RequestParam(required = false) String search) {
        
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            // Search by name - LẤY TẤT CẢ (bao gồm cả isDelete = true)
            Page<Product> productPage = adminProductRepository.findByNameContainingIgnoreCase(
                search.trim(), org.springframework.data.domain.PageRequest.of(0, 100));
            products = productPage.getContent();
        } else {
            // Get all products - LẤY TẤT CẢ (bao gồm cả isDelete = true)
            products = adminProductRepository.findAll();
        }
        
        // Convert to DTO và sắp xếp theo stock tăng dần (sản phẩm hết hàng lên đầu)
        List<AdminProductDTO> dtos = products.stream()
                .map(this::toDTO)
                .sorted((p1, p2) -> {
                    int stock1 = p1.getStock() != null ? p1.getStock() : 0;
                    int stock2 = p2.getStock() != null ? p2.getStock() : 0;
                    return Integer.compare(stock1, stock2); // Tăng dần: 0, 1, 2, ...
                })
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
        
        // Handle images - prioritize 'images' list, fallback to 'image' single
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            // Use images list if provided
            product.setImages(dto.getImages());
        } else if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            // Fallback to single image
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
                    
                    // Handle images - prioritize 'images' list, fallback to 'image' single
                    if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                        // Use images list if provided
                        existingProduct.setImages(dto.getImages());
                    } else if (dto.getImage() != null && !dto.getImage().isEmpty()) {
                        // Fallback to single image
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
     * GET /api/admin/products/stats?threshold=10
     */
    @GetMapping("/stats")
    public ResponseEntity<ProductStatsDTO> getProductStats(
            @RequestParam(defaultValue = "10") int threshold) {
        // ĐẾM CHỈ SẢN PHẨM ACTIVE (isDelete = false) cho stats
        List<Product> activeProducts = adminProductRepository.findByIsDeleteFalse();
        
        int total = activeProducts.size();
        int outOfStock = (int) activeProducts.stream()
                .filter(p -> p.getStock() == null || p.getStock() == 0)
                .count();
        int lowStock = (int) activeProducts.stream()
                .filter(p -> p.getStock() != null && p.getStock() > 0 && p.getStock() <= threshold)
                .count();
        
        log.info("✅ Product stats calculated with threshold={}: total={}, lowStock={}, outOfStock={}",
                threshold, total, lowStock, outOfStock);
        
        ProductStatsDTO stats = ProductStatsDTO.builder()
                .total(total)
                .outOfStock(outOfStock)
                .lowStock(lowStock)
                .build();
        
        return ok(stats);
    }
}