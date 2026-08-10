package com.pearlnails.backend.servicecatalog.service.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.pearlnails.backend.servicecatalog.service.Service;

public record ServiceResponse(
    Long id,
    Long categoryId,
    String categoryName,
    String name,
    String description,
    BigDecimal price,
    Integer durationMinutes,
    Integer bufferMinutes,
    Integer displayOrder,
    Boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

    public static ServiceResponse from(Service service) {
        return new ServiceResponse(
            service.getId(),
            service.getCategory().getId(),
            service.getCategory().getName(),
            service.getName(),
            service.getDescription(),
            service.getPrice(),
            service.getDurationMinutes(),
            service.getBufferMinutes(),
            service.getDisplayOrder(),
            service.getActive(),
            service.getCreatedAt(),
            service.getUpdatedAt()
        );
    }
}