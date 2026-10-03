package com.pearlnails.backend.employee.dto;

import java.time.OffsetDateTime;

import com.pearlnails.backend.employee.Employee;

public record EmployeeResponse(
    Long id,
    String firstName,
    String lastName,
    String displayName,
    String phone,
    String email,
    Integer displayOrder,
    Boolean active,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getDisplayName(),
            employee.getPhone(),
            employee.getEmail(),
            employee.getDisplayOrder(),
            employee.getActive(),
            employee.getCreatedAt(),
            employee.getUpdatedAt()
        );
    }
}
