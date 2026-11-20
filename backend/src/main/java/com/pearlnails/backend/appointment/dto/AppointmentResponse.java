package com.pearlnails.backend.appointment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.pearlnails.backend.appointment.Appointment;
import com.pearlnails.backend.appointment.AppointmentStatus;

public record AppointmentResponse(

    Long id,
    Long employeeId,
    Long serviceId,

    String customerName,
    String customerPhone,
    String customerEmail,

    OffsetDateTime startsAt,
    OffsetDateTime endsAt,
    OffsetDateTime reservedUntil,

    String serviceName,
    BigDecimal priceAtBooking,
    Integer durationMinutes,
    Integer bufferMinutes,

    AppointmentStatus status

) {

    public static AppointmentResponse from(
        Appointment appointment
    ) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getEmployee().getId(),
            appointment.getService().getId(),

            appointment.getCustomerName(),
            appointment.getCustomerPhone(),
            appointment.getCustomerEmail(),

            appointment.getStartsAt(),
            appointment.getEndsAt(),
            appointment.getReservedUntil(),

            appointment.getServiceName(),
            appointment.getPriceAtBooking(),
            appointment.getDurationMinutes(),
            appointment.getBufferMinutes(),

            appointment.getStatus()
        );
    }
}
