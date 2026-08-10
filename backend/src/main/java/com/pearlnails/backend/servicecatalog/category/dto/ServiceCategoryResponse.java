package com.pearlnails.backend.servicecatalog.category.dto;

import java.time.OffsetDateTime;

import com.pearlnails.backend.servicecatalog.category.ServiceCategory;

public record ServiceCategoryResponse(
    Long id,
    String name,
    String description,
    Integer displayOrder,
    Boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

    public static ServiceCategoryResponse from(ServiceCategory category) {
        return new ServiceCategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getDisplayOrder(),
            category.getActive(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}