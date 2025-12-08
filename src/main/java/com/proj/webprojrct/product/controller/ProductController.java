package com.proj.webprojrct.product.controller;

import com.proj.webprojrct.product.dto.*;
import com.proj.webprojrct.category.dto.CategoryDto;
import com.proj.webprojrct.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    // Endpoint cũ - giữ lại để backward compatibility
    @GetMapping("/products")
    public ResponseEntity<Object> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        var products = productService.searchProducts(name, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    // Endpoint mới với bộ lọc nâng cao - có phân trang
    @GetMapping("/products/filter")
    public ResponseEntity<ProductPageDto> getProductsWithFilter(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String productSize,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        
        ProductFilterDto filter = new ProductFilterDto();
        filter.setName(name);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setCategoryIds(categoryIds);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);
        filter.setPage(page);
        filter.setPageSize(pageSize);

        ProductPageDto result = productService.getProductsWithFilters(filter);
        return ResponseEntity.ok(result);
    }

    // Endpoint với bộ lọc không phân trang
    @GetMapping("/products/search")
    public ResponseEntity<List<ProductListDto>> searchProductsAdvanced(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String productSize,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Long categoryId) {
        
        ProductFilterDto filter = new ProductFilterDto();
        filter.setName(name);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);

        List<ProductListDto> result = productService.getProductsWithFiltersNoPage(filter);
        return ResponseEntity.ok(result);
    }

    // Endpoint với POST body để gửi filter phức tạp
    @PostMapping("/products/filter")
    public ResponseEntity<ProductPageDto> getProductsWithFilterBody(@RequestBody ProductFilterDto filter) {
        ProductPageDto result = productService.getProductsWithFilters(filter);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(dto));
    }

    @PatchMapping("/products/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @RequestBody ProductUpdateDto dto,
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Endpoint để lấy sản phẩm nổi bật
    @GetMapping("/products/featured")
    public ResponseEntity<List<ProductListDto>> getFeaturedProducts(
            @RequestParam(required = false, defaultValue = "8") Integer limit) {
        List<ProductListDto> result = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(result);
    }

    // Endpoint để lấy danh sách brands
    @GetMapping("/products/brands")
    public ResponseEntity<List<String>> getBrands() {
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    // Endpoint để lấy danh sách categories
    @GetMapping("/products/categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        List<CategoryDto> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // Endpoint để search suggestion
    @GetMapping("/products/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        List<String> suggestions = productService.getSearchSuggestions(query, limit);
        return ResponseEntity.ok(suggestions);
    }
}
