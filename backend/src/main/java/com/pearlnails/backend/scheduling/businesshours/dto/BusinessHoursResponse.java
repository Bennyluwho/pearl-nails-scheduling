package com.pearlnails.backend.scheduling.businesshours.dto;

import java.time.LocalTime;

import com.pearlnails.backend.scheduling.businesshours.BusinessHours;

public record BusinessHoursResponse(
    Long id,
    Integer dayOfWeek,
    LocalTime openTime,
    LocalTime closeTime,
    Boolean closed
) {
    public static BusinessHoursResponse from(
        BusinessHours hours
    ) {
        return new BusinessHoursResponse(
            hours.getId(),
            hours.getDayOfWeek(),
            hours.getOpenTime(),
            hours.getCloseTime(),
            hours.getClosed()
        );
    }
}
