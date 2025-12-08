package com.proj.webprojrct.order.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private List<String> productImages;
    private Integer quantity;
    private Double productPrice;
    private Double totalPrice;
    private String size;
    private boolean reviewed;
    private ProductOrderDTO product;
    private ReviewOrderDTO review;
}
