package com.pearlnails.backend.scheduling.workinghours.dto;

import java.time.LocalTime;

import com.pearlnails.backend.scheduling.workinghours.EmployeeWorkingHours;

public record EmployeeWorkingHoursResponse(
    Long id,
    Long employeeId,
    Integer dayOfWeek,
    LocalTime startTime,
    LocalTime endTime
) {

    public static EmployeeWorkingHoursResponse from(
        EmployeeWorkingHours workingHours
    ) {
        return new EmployeeWorkingHoursResponse(
            workingHours.getId(),
            workingHours.getEmployee().getId(),
            workingHours.getDayOfWeek(),
            workingHours.getStartTime(),
            workingHours.getEndTime()
        );
    }
}