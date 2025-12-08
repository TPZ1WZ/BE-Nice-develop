package com.proj.webprojrct.cart.dto;

import com.proj.webprojrct.cart.entity.Cart;
import com.proj.webprojrct.cart.entity.CartItem;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items = new ArrayList<>();
    private Double totalPrice = 0.0;
    private Integer totalQuantity = 0;

    public CartResponse(Cart cart) {
        this.id = cart.getId();
        
        // Calculate totals from items instead of using stored values
        double calculatedTotalPrice = 0.0;
        int calculatedTotalQuantity = 0;
        
        for (CartItem item : cart.getItems()) {
            ProductInCart productInCart = new ProductInCart(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getSlug(),
                    item.getProduct().getSubTitle(),
                    item.getProduct().getDescription(),
                    item.getProduct().getPrice(),
                    item.getProduct().getImages()
            );
            CartItemResponse cartItemResponse = new CartItemResponse(
                    item.getId(),
                    productInCart,
                    item.getQuantity(),
                    item.getSize(),
                    item.getProductPrice(),
                    item.getTotalPrice()
            );
            this.items.add(cartItemResponse);
            
            // Add to calculated totals
            calculatedTotalPrice += item.getTotalPrice();
            calculatedTotalQuantity += item.getQuantity();
        }
        
        // Use calculated values instead of stored ones
        this.totalPrice = calculatedTotalPrice;
        this.totalQuantity = calculatedTotalQuantity;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class CartItemResponse {
        private Long id;
        private ProductInCart product;
        private Integer quantity;
        private String size;
        private Double productPrice;
        private Double totalPrice;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ProductInCart {
        private Long id;
        private String name;
        private String slug;
        private String subTitle;
        private String description;
        private Double price;
        private List<String> images;
    }
}
