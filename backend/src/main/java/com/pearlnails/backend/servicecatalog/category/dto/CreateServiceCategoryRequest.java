package com.pearlnails.backend.servicecatalog.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateServiceCategoryRequest(

    @NotBlank
    @Size(max = 100)
    String name,

    @Size(max = 500)
    String description,

    @PositiveOrZero
    Integer displayOrder
) {
}