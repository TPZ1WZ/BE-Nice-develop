package com.proj.webprojrct.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for review-related exceptions
 */
@RestControllerAdvice
public class ReviewExceptionHandler {

    @ExceptionHandler(InappropriateContentException.class)
    public ResponseEntity<Map<String, Object>> handleInappropriateContent(InappropriateContentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Inappropriate Content");
        body.put("message", "Bình luận của bạn chứa nội dung không phù hợp và không thể đăng");
        body.put("reason", ex.getReason());
        body.put("suggestion", "Vui lòng viết bình luận lịch sự và mang tính xây dựng");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
