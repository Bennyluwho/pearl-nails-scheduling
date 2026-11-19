
package com.pearlnails.backend.appointment;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pearlnails.backend.common.exception.InvalidOperationException;
import com.pearlnails.backend.common.exception.ResourceNotFoundException;

import com.pearlnails.backend.employee.Employee;
import com.pearlnails.backend.employee.EmployeeRepository;

import com.pearlnails.backend.employeeassignment.EmployeeServiceAssignmentRepository;

import com.pearlnails.backend.servicecatalog.service.ServiceRepository;

import com.pearlnails.backend.scheduling.businesshours.BusinessHours;
import com.pearlnails.backend.scheduling.businesshours.BusinessHoursRepository;

import com.pearlnails.backend.scheduling.workinghours.EmployeeWorkingHoursRepository;

import com.pearlnails.backend.scheduling.timeoff.EmployeeTimeOffRepository;

@Service
public class AppointmentAvailabilityService {

    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;
    private final EmployeeServiceAssignmentRepository assignmentRepository;
    private final BusinessHoursRepository businessHoursRepository;
    private final EmployeeWorkingHoursRepository workingHoursRepository;
    private final EmployeeTimeOffRepository timeOffRepository;
    private final ZoneId salonZone;

    public AppointmentAvailabilityService(
        AppointmentRepository appointmentRepository,
        EmployeeRepository employeeRepository,
        ServiceRepository serviceRepository,
        EmployeeServiceAssignmentRepository assignmentRepository,
        BusinessHoursRepository businessHoursRepository,
        EmployeeWorkingHoursRepository workingHoursRepository,
        EmployeeTimeOffRepository timeOffRepository,
        @Value("${app.salon.time-zone:America/Los_Angeles}")
        String salonTimeZone
    ) {
        this.appointmentRepository = appointmentRepository;
        this.employeeRepository = employeeRepository;
        this.serviceRepository = serviceRepository;
        this.assignmentRepository = assignmentRepository;
        this.businessHoursRepository = businessHoursRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.timeOffRepository = timeOffRepository;
        this.salonZone = ZoneId.of(salonTimeZone);
    }

    // The appointment service will reuse this result.
    public record AvailableSlot(
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        OffsetDateTime reservedUntil
    ) {}

    @Transactional(readOnly = true)
    public AvailableSlot validateAvailability(
        Long employeeId,
        Long serviceId,
        LocalDate date,
        LocalTime startTime
    ) {
        // STEP 1: Validate the input and employee.

        if (employeeId == null || serviceId == null
            || date == null || startTime == null) {
            throw new InvalidOperationException(
                "Employee, service, date, and start time are required"
            );
        }

        Employee employee = employeeRepository
            .findById(employeeId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Employee not found: " + employeeId
                )
            );

        if (!Boolean.TRUE.equals(employee.getActive())) {
            throw new InvalidOperationException(
                "Employee is inactive"
            );
        }

        // STEP 2: Validate the service and qualification.

        com.pearlnails.backend.servicecatalog.service.Service
            salonService = serviceRepository
                .findById(serviceId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Service not found: " + serviceId
                    )
                );

        if (!Boolean.TRUE.equals(salonService.getActive())
            || !Boolean.TRUE.equals(
                salonService.getCategory().getActive()
            )) {
            throw new InvalidOperationException(
                "Service is inactive"
            );
        }

        if (!assignmentRepository
            .existsByEmployee_IdAndService_Id(
                employeeId, serviceId
            )) {
            throw new InvalidOperationException(
                "Employee is not assigned to this service"
            );
        }

        int durationMinutes = salonService.getDurationMinutes();
        int bufferMinutes = salonService.getBufferMinutes();

        if (durationMinutes <= 0 || bufferMinutes < 0) {
            throw new InvalidOperationException(
                "Service duration or buffer is invalid"
            );
        }

        // STEP 3: Calculate appointment timestamps.

        LocalDateTime localStart =
            LocalDateTime.of(date, startTime);

        // Reject nonexistent or ambiguous local times
        // during daylight-saving clock changes.
        List<ZoneOffset> validOffsets =
            salonZone.getRules().getValidOffsets(localStart);

        if (validOffsets.size() != 1) {
            throw new InvalidOperationException(
                "Selected time is invalid or ambiguous in salon timezone"
            );
        }

        ZonedDateTime startsAt = ZonedDateTime.ofStrict(
            localStart,
            validOffsets.get(0),
            salonZone
        );

        if (!startsAt.toInstant().isAfter(Instant.now())) {
            throw new InvalidOperationException(
                "Appointments must start in the future"
            );
        }

        ZonedDateTime endsAt =
            startsAt.plusMinutes(durationMinutes);

        ZonedDateTime reservedUntil =
            endsAt.plusMinutes(bufferMinutes);

        // The existing business-hours model is
        // limited to same-day opening and closing.
        if (!reservedUntil.toLocalDate().equals(date)) {
            throw new InvalidOperationException(
                "Appointment cannot extend into the next day"
            );
        }

        LocalTime reservedEndTime =
            reservedUntil.toLocalTime();

        int dayOfWeek = date.getDayOfWeek().getValue();

        // STEP 4: Check salon business hours.

        BusinessHours businessHours = businessHoursRepository
            .findByDayOfWeek(dayOfWeek)
            .orElseThrow(() ->
                new InvalidOperationException(
                    "Salon hours are not configured for this day"
                )
            );

        if (Boolean.TRUE.equals(businessHours.getClosed())
            || businessHours.getOpenTime() == null
            || businessHours.getCloseTime() == null) {
            throw new InvalidOperationException(
                "Salon is closed on this day"
            );
        }

        if (startTime.isBefore(businessHours.getOpenTime())
            || reservedEndTime.isAfter(
                businessHours.getCloseTime()
            )) {
            throw new InvalidOperationException(
                "Appointment is outside salon business hours"
            );
        }

        // STEP 5: Check employee weekly shifts.

        boolean withinShift = workingHoursRepository
            .findAllByEmployee_IdAndDayOfWeekOrderByStartTimeAsc(
                employeeId,
                dayOfWeek
            )
            .stream()
            .anyMatch(shift ->
                !startTime.isBefore(shift.getStartTime())
                && !reservedEndTime.isAfter(shift.getEndTime())
            );

        if (!withinShift) {
            throw new InvalidOperationException(
                "Appointment is outside employee working hours"
            );
        }

        // STEP 6: Check employee time off.

        boolean onTimeOff = timeOffRepository
            .findAllByEmployee_IdAndTimeOffDateOrderByStartTimeAsc(
                employeeId,
                date
            )
            .stream()
            .anyMatch(timeOff ->
                Boolean.TRUE.equals(timeOff.getFullDay())
                || (
                    startTime.isBefore(timeOff.getEndTime())
                    && reservedEndTime.isAfter(
                        timeOff.getStartTime()
                    )
                )
            );

        if (onTimeOff) {
            throw new InvalidOperationException(
                "Employee has time off during this appointment"
            );
        }

        // STEP 7: Check existing appointments.

        boolean hasConflict = !appointmentRepository
            .findAllByEmployee_IdAndStartsAtLessThanAndReservedUntilGreaterThanAndStatusNotOrderByStartsAtAsc(
                employeeId,
                reservedUntil.toOffsetDateTime(),
                startsAt.toOffsetDateTime(),
                AppointmentStatus.CANCELLED
            )
            .isEmpty();

        if (hasConflict) {
            throw new InvalidOperationException(
                "Employee already has an appointment during this time"
            );
        }

        // All checks passed.
        return new AvailableSlot(
            startsAt.toOffsetDateTime(),
            endsAt.toOffsetDateTime(),
            reservedUntil.toOffsetDateTime()
        );
    }
}
