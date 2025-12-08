package com.proj.webprojrct.category.repo;

import com.proj.webprojrct.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    List<Category> findByName(String name);
    List<Category> findByDescription(String description);
    List<Category> findByIsDelete(Boolean isDelete);
}
