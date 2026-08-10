package com.pearlnails.backend.servicecatalog.category;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pearlnails.backend.servicecatalog.category.dto.CreateServiceCategoryRequest;
import com.pearlnails.backend.servicecatalog.category.dto.ServiceCategoryResponse;
import com.pearlnails.backend.servicecatalog.category.dto.UpdateServiceCategoryRequest;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository categoryRepository;

    public ServiceCategoryService(
        ServiceCategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceCategoryResponse> getAllCategories() {
        return categoryRepository
            .findAllByOrderByDisplayOrderAscNameAsc()
            .stream()
            .map(ServiceCategoryResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ServiceCategoryResponse getCategoryById(Long id) {
        ServiceCategory category = findCategoryById(id);

        return ServiceCategoryResponse.from(category);
    }

    @Transactional
    public ServiceCategoryResponse createCategory(
        CreateServiceCategoryRequest request
    ) {
        String normalizedName = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException(
                "A service category with that name already exists"
            );
        }

        int displayOrder =
            request.displayOrder() != null
                ? request.displayOrder()
                : 0;

        ServiceCategory category = new ServiceCategory(
            normalizedName,
            request.description(),
            displayOrder
        );

        ServiceCategory saved = categoryRepository.save(category);

        return ServiceCategoryResponse.from(saved);
    }

    @Transactional
    public ServiceCategoryResponse updateCategory(
        Long id,
        UpdateServiceCategoryRequest request
    ) {
        ServiceCategory category = findCategoryById(id);

        if (request.name() != null) {
            String normalizedName = request.name().trim();

            categoryRepository
                .findByNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                        "A service category with that name already exists"
                    );
                });

            category.setName(normalizedName);
        }

        if (request.description() != null) {
            category.setDescription(request.description());
        }

        if (request.displayOrder() != null) {
            category.setDisplayOrder(request.displayOrder());
        }

        if (request.active() != null) {
            category.setActive(request.active());
        }

        return ServiceCategoryResponse.from(category);
    }

    @Transactional
    public void deactivateCategory(Long id) {
        ServiceCategory category = findCategoryById(id);
        category.setActive(false);
    }

    private ServiceCategory findCategoryById(Long id) {
        return categoryRepository
            .findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Service category not found: " + id
                )
            );
    }
}