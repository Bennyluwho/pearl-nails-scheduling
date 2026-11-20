package com.pearlnails.backend.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAppointmentRequest(
    @NotNull
    @Positive
    Long employeeId,

    @NotNull
    @Positive 
    Long serviceId,

    @NotBlank 
    @Size(max = 150)
    String customerName,

    @NotBlank
    @Size(max = 30)
    String customerPhone,

    @Email
    @Size(max = 255)
    String customerEmail,

    @NotNull
    LocalDate appointmentDate,

    @NotNull 
    LocalTime startTime

) {}
