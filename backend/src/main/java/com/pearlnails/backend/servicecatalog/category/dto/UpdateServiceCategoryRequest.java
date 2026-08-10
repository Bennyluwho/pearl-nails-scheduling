package com.pearlnails.backend.servicecatalog.category.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateServiceCategoryRequest(

    @Size(min = 1, max = 100)
    String name,

    @Size(max = 500)
    String description,

    @PositiveOrZero
    Integer displayOrder,
    
    Boolean active

) {
}