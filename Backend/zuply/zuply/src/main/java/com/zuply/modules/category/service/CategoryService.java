package com.zuply.modules.category.service;

import com.zuply.modules.category.model.Category;
import com.zuply.modules.category.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @PostConstruct
    public void seedCategories() {
        if (categoryRepository.count() > 0) return; // already seeded

        List<String[]> categories = Arrays.asList(
                new String[]{"Food & Beverage",        "food-beverage"},
                new String[]{"Grocery",                "grocery"},
                new String[]{"Fashion & Footwear",     "fashion-footwear"},
                new String[]{"Home & Kitchen",         "home-kitchen"},
                new String[]{"Electronics",            "electronics"},
                new String[]{"Beauty & Personal Care", "beauty-personal-care"},
                new String[]{"Health & Wellness",      "health-wellness"},
                new String[]{"Agriculture",            "agriculture"}
        );

        for (String[] pair : categories) {
            Category cat = new Category();
            cat.setName(pair[0]);
            cat.setSlug(pair[1]);
            categoryRepository.save(cat);
        }
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}
