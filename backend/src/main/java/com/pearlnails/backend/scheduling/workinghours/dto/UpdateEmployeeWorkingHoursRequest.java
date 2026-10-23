package com.pearlnails.backend.scheduling.workinghours.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateEmployeeWorkingHoursRequest(

    @Min(1)
    @Max(7)
    Integer dayOfWeek,

    LocalTime startTime,

    LocalTime endTime
    
) {}
