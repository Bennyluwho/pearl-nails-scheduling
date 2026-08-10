package com.pearlnails.backend.servicecatalog.service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateServiceRequest(

    @NotNull
    Long categoryId,

    @NotBlank
    @Size(max = 150)
    String name,

    @Size(max = 1000)
    String description,

    @NotNull
    @DecimalMin(value = "0.00")
    BigDecimal price,

    @NotNull
    @Positive
    Integer durationMinutes,

    @PositiveOrZero
    Integer bufferMinutes,

    @PositiveOrZero
    Integer displayOrder

) {
}