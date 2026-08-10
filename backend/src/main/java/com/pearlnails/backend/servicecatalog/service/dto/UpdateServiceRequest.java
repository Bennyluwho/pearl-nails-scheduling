package com.pearlnails.backend.servicecatalog.service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateServiceRequest(

    Long categoryId,

    @Size(min = 1, max = 150)
    String name,

    @Size(max = 1000)
    String description,

    @DecimalMin(value = "0.00")
    BigDecimal price,

    @Positive
    Integer durationMinutes,

    @PositiveOrZero
    Integer bufferMinutes,

    @PositiveOrZero
    Integer displayOrder,

    Boolean active

) {
}