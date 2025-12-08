package com.proj.webprojrct.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewOrderDTO {
    private Integer rating; // Đánh giá từ 1-5 sao
    private String comment; // Nội dung bình luận
    private String title; // Tiêu đề đánh giá (tùy chọn)
    private List<String> images;
}
