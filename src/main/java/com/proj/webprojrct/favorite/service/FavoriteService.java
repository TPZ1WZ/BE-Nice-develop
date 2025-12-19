package com.proj.webprojrct.favorite.service;

import com.proj.webprojrct.common.exception.EntityNotExistException;
import com.proj.webprojrct.favorite.dto.FavoriteResponseDto;
import com.proj.webprojrct.favorite.entity.Favorite;
import com.proj.webprojrct.favorite.repository.FavoriteRepository;
import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.product.repository.ProductRepository;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    /**
     * Thêm sản phẩm vào danh sách yêu thích
     */
    @Transactional
    public FavoriteResponseDto addToFavorites(Long userId, Long productId) {
        // Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotExistException("User not found with id: " + userId));
        
        // Kiểm tra product tồn tại
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotExistException("Product not found with id: " + productId));
        
        // Kiểm tra đã tồn tại chưa
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("Product already in favorites");
        }
        
        // Tạo favorite mới
        Favorite favorite = Favorite.builder()
            .user(user)
            .product(product)
            .build();
        
        Favorite saved = favoriteRepository.save(favorite);
        
        return mapToDto(saved);
    }
    
    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     */
    @Transactional
    public void removeFromFavorites(Long userId, Long productId) {
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId)
            .orElseThrow(() -> new EntityNotExistException("Favorite not found"));
        
        favoriteRepository.delete(favorite);
    }
    
    /**
     * Lấy danh sách sản phẩm yêu thích (có phân trang)
     */
    @Transactional(readOnly = true)
    public Page<FavoriteResponseDto> getUserFavorites(Long userId, Pageable pageable) {
        // Kiểm tra user tồn tại
        if (!userRepository.existsById(userId)) {
            throw new EntityNotExistException("User not found with id: " + userId);
        }
        
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::mapToDto);
    }
    
    /**
     * Kiểm tra sản phẩm đã được yêu thích chưa
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
    
    /**
     * Đếm số lượng sản phẩm yêu thích
     */
    @Transactional(readOnly = true)
    public long countUserFavorites(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }
    
    /**
     * Lấy danh sách product IDs mà user đã yêu thích
     */
    @Transactional(readOnly = true)
    public List<Long> getUserFavoriteProductIds(Long userId) {
        return favoriteRepository.findProductIdsByUserId(userId);
    }
    
    /**
     * Map Favorite entity sang DTO
     */
    private FavoriteResponseDto mapToDto(Favorite favorite) {
        Product product = favorite.getProduct();
        
        return FavoriteResponseDto.builder()
            .id(favorite.getId())
            .productId(product.getId())
            .name(product.getName())
            .slug(product.getSlug())
            .subTitle(product.getSubTitle())
            .price(product.getPrice())
            .stock(product.getStock())
            .images(product.getImages())
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
            .addedAt(favorite.getCreatedAt())
            .build();
    }
}
