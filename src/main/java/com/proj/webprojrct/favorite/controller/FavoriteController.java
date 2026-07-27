package com.proj.webprojrct.favorite.controller;

import com.proj.webprojrct.common.utils.SecurityUtil;
import com.proj.webprojrct.favorite.dto.FavoriteResponseDto;
import com.proj.webprojrct.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "API quản lý danh sách yêu thích")
@SecurityRequirement(name = "Bearer Authentication")
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * Thêm sản phẩm vào danh sách yêu thích
     * POST /api/v1/favorites/{productId}
     */
    @PostMapping("/{productId}")
    @Operation(summary = "Thêm sản phẩm vào yêu thích")
    public ResponseEntity<Map<String, Object>> addToFavorites(@PathVariable Long productId) {
        Long userId = SecurityUtil.getCurrentUserId();

        FavoriteResponseDto favorite = favoriteService.addToFavorites(userId, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product added to favorites");
        response.put("data", favorite);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     * DELETE /api/v1/favorites/{productId}
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "Xóa sản phẩm khỏi yêu thích")
    public ResponseEntity<Map<String, Object>> removeFromFavorites(@PathVariable Long productId) {
        Long userId = SecurityUtil.getCurrentUserId();

        favoriteService.removeFromFavorites(userId, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product removed from favorites");

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách sản phẩm yêu thích
     * GET /api/v1/favorites?page=0&size=20
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm yêu thích")
    public ResponseEntity<Map<String, Object>> getUserFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtil.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size);
        Page<FavoriteResponseDto> favorites = favoriteService.getUserFavorites(userId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", favorites.getContent());
        response.put("currentPage", favorites.getNumber());
        response.put("totalPages", favorites.getTotalPages());
        response.put("totalItems", favorites.getTotalElements());

        return ResponseEntity.ok(response);
    }

    /**
     * Kiểm tra sản phẩm đã được yêu thích chưa
     * GET /api/v1/favorites/check/{productId}
     */
    @GetMapping("/check/{productId}")
    @Operation(summary = "Kiểm tra sản phẩm đã yêu thích chưa")
    public ResponseEntity<Map<String, Object>> checkFavorite(@PathVariable Long productId) {
        Long userId = SecurityUtil.getCurrentUserId();

        boolean isFavorite = favoriteService.isFavorite(userId, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isFavorite", isFavorite);

        return ResponseEntity.ok(response);
    }

    /**
     * Đếm số lượng sản phẩm yêu thích
     * GET /api/v1/favorites/count
     */
    @GetMapping("/count")
    @Operation(summary = "Đếm số sản phẩm yêu thích")
    public ResponseEntity<Map<String, Object>> countFavorites() {
        Long userId = SecurityUtil.getCurrentUserId();

        long count = favoriteService.countUserFavorites(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách product IDs đã yêu thích
     * GET /api/v1/favorites/product-ids
     */
    @GetMapping("/product-ids")
    @Operation(summary = "Lấy danh sách product IDs đã yêu thích")
    public ResponseEntity<Map<String, Object>> getFavoriteProductIds() {
        Long userId = SecurityUtil.getCurrentUserId();

        List<Long> productIds = favoriteService.getUserFavoriteProductIds(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("productIds", productIds);

        return ResponseEntity.ok(response);
    }
}
