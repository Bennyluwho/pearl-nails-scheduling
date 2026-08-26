package com.pearlnails.backend.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(

    @NotBlank
    @Size(max = 100)
    String firstName,

    @NotBlank
    @Size(max = 100)
    String lastName,

    @Size(max = 150)
    String displayName,

    @Size(max = 30)
    String phone,

    @Email
    @Size(max = 255)
    String email,

    @PositiveOrZero
    Integer displayOrder
    
) {
}
