package com.pearlnails.backend.scheduling.businesshours.dto;

import java.time.LocalTime;
import jakarta.validation.constraints.NotNull;

public record UpdateBusinessHoursRequest(
    @NotNull Boolean closed,
    LocalTime openTime,
    LocalTime closeTime
) {}
