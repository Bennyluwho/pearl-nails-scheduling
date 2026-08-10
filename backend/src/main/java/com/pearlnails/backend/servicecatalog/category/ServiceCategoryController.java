package com.pearlnails.backend.servicecatalog.category;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pearlnails.backend.servicecatalog.category.dto.CreateServiceCategoryRequest;
import com.pearlnails.backend.servicecatalog.category.dto.ServiceCategoryResponse;
import com.pearlnails.backend.servicecatalog.category.dto.UpdateServiceCategoryRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/service-categories")
public class ServiceCategoryController {

    private final ServiceCategoryService categoryService;

    public ServiceCategoryController(
        ServiceCategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<ServiceCategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public ServiceCategoryResponse getCategoryById(
        @PathVariable Long id
    ) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping
    public ResponseEntity<ServiceCategoryResponse> createCategory(
        @Valid @RequestBody CreateServiceCategoryRequest request
    ) {
        ServiceCategoryResponse created =
            categoryService.createCategory(request);

        return ResponseEntity
            .created(
                URI.create(
                    "/api/service-categories/" + created.id()
                )
            )
            .body(created);
    }

    @PatchMapping("/{id}")
    public ServiceCategoryResponse updateCategory(
        @PathVariable Long id,
        @Valid @RequestBody UpdateServiceCategoryRequest request
    ) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCategory(
        @PathVariable Long id
    ) {
        categoryService.deactivateCategory(id);

        return ResponseEntity.noContent().build();
    }
}