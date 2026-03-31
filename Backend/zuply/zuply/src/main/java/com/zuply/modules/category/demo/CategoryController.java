package com.zuply.modules.category.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.category.model.Category;
import com.zuply.modules.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        return ResponseEntity.ok(
                ApiResponse.success(categories, "Categories fetched"));
    }
}