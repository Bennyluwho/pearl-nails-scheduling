package com.pearlnails.backend.scheduling.workinghours.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateEmployeeWorkingHoursRequest(
    @NotNull
    @Min(1)
    @Max(7)
    Integer dayOfWeek,

    @NotNull
    LocalTime startTime,

    @NotNull
    LocalTime endTime

) {}