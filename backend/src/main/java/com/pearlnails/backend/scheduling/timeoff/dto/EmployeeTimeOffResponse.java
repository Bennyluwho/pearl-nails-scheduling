package com.pearlnails.backend.scheduling.timeoff.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.pearlnails.backend.scheduling.timeoff.EmployeeTimeOff;

public record EmployeeTimeOffResponse(
    Long id,
    Long employeeId,
    LocalDate timeOffDate,
    Boolean fullDay,
    LocalTime startTime,
    LocalTime endTime,
    String reason
) {
    
    public static EmployeeTimeOffResponse from(
        EmployeeTimeOff timeOff
    ) {
        return new EmployeeTimeOffResponse(
            timeOff.getId(),
            timeOff.getEmployee().getId(),
            timeOff.getTimeOffDate(),
            timeOff.getFullDay(),
            timeOff.getStartTime(),
            timeOff.getEndTime(),
            timeOff.getReason()
        );
    }
}
