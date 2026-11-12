package com.pearlnails.backend.scheduling.timeoff.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Size;

public record UpdateEmployeeTimeOffRequest(

    LocalDate timeOffDate,

    Boolean fullDay,

    LocalTime startTime,

    LocalTime endTime,

    @Size(max = 255)
    String reason

) {}
