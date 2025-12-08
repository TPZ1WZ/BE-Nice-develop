package com.proj.webprojrct.category.controller;

import com.proj.webprojrct.category.entity.Category;
import com.proj.webprojrct.category.repo.CategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepo categoryRepo;
    @GetMapping
    public String getCategoriesPage(Model model) {
        model.addAttribute("categories", categoryRepo.findByIsDelete(false));
        return "admin/category";
    }

    @GetMapping("create")
    public String getCreateCategoryPage(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-save";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            var category = categoryRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
            model.addAttribute("category", category);
            return "admin/category-save";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Không tìm thấy danh mục!");
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            var category = categoryRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
            category.setIsDelete(true);
            categoryRepo.save(category);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi xóa danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                               RedirectAttributes redirectAttributes) {
        try {
            if (category.getId() != null) {
                Category existing = categoryRepo.findById(category.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + category.getId()));

                existing.setName(category.getName());
                existing.setDescription(category.getDescription());
                existing.setUpdatedAt(java.time.LocalDateTime.now());
                existing.setIsDelete(category.getIsDelete() != null ? category.getIsDelete() : false);

                categoryRepo.save(existing);
                redirectAttributes.addFlashAttribute("successMessage", "✅ Cập nhật danh mục thành công!");
            } else {
                category.setCreatedAt(java.time.LocalDateTime.now());
                category.setUpdatedAt(java.time.LocalDateTime.now());
                category.setIsDelete(false);
                categoryRepo.save(category);
                redirectAttributes.addFlashAttribute("successMessage", "✅ Thêm danh mục mới thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi lưu danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
