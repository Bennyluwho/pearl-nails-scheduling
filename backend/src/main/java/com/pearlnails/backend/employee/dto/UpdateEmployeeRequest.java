package com.pearlnails.backend.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequest(
    @Size(min = 1, max = 100)
    String firstName,
    
    @Size(min = 1, max = 100)
    String lastName,

    @Size(max = 150)
    String displayName,

    @Size(max = 30)
    String phone,

    @Email
    @Size(max = 255)
    String email,

    @PositiveOrZero
    Integer displayOrder,

    Boolean active

) {
}
