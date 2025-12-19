package com.proj.webprojrct.category.controller;

import com.proj.webprojrct.category.entity.Category;
import com.proj.webprojrct.category.repo.CategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class ApiCategoryController {

    private final CategoryRepo categoryRepo;

    @GetMapping
    public ResponseEntity<List<Category>> getCategories() {
        List<Category> categories = categoryRepo.findByIsDelete(false);
        return ResponseEntity.ok(categories);
    }
}
