package com.pearlnails.backend.scheduling.timeoff;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pearlnails.backend.common.exception.InvalidOperationException;
import com.pearlnails.backend.common.exception.ResourceNotFoundException;

import com.pearlnails.backend.employee.Employee;
import com.pearlnails.backend.employee.EmployeeRepository;

import com.pearlnails.backend.scheduling.timeoff.dto.CreateEmployeeTimeOffRequest;
import com.pearlnails.backend.scheduling.timeoff.dto.UpdateEmployeeTimeOffRequest;
import com.pearlnails.backend.scheduling.timeoff.dto.EmployeeTimeOffResponse;

@Service 
public class EmployeeTimeOffService {
    
    private final EmployeeTimeOffRepository timeOffRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeTimeOffService(
        EmployeeTimeOffRepository timeOffRepository,
        EmployeeRepository employeeRepository
    ) {
        this.timeOffRepository = timeOffRepository;
        this.employeeRepository = employeeRepository;
    }

    // READ: All absences belonging to an employee

    @Transactional(readOnly = true)
    public List<EmployeeTimeOffResponse> getTimeOff(
        Long employeeId
    ) {
        findEmployee(employeeId);

        return timeOffRepository
            .findAllByEmployee_IdOrderByTimeOffDateAscStartTimeAsc(
                employeeId
            )
            .stream()
            .map(EmployeeTimeOffResponse::from)
            .toList();
    }

    // CREATE: Record a new absence

    @Transactional
    public EmployeeTimeOffResponse createTimeOff(
        Long employeeId,
        CreateEmployeeTimeOffRequest request
    ) {
        Employee employee = findEmployee(employeeId);

        if (!Boolean.TRUE.equals(employee.getActive())) {
            throw new InvalidOperationException(
                "Cannot create time off for an inactive employee"
            );
        }

        validateTimeOff(
            request.timeOffDate(),
            request.fullDay(),
            request.startTime(),
            request.endTime()
        );

        validateNoConflicts(
            employeeId,
            request.timeOffDate(),
            request.fullDay(),
            request.startTime(),
            request.endTime(),
            null
        );

        EmployeeTimeOff timeOff = new EmployeeTimeOff(
            employee,
            request.timeOffDate(),
            request.fullDay(),
            request.startTime(),
            request.endTime(),
            normalizeReason(request.reason())
        );

        return EmployeeTimeOffResponse.from(
            timeOffRepository.save(timeOff)
        );
    }

    // UPDATE: Modify an existing absence

    @Transactional
    public EmployeeTimeOffResponse updateTimeOff(
        Long employeeId,
        Long timeOffId,
        UpdateEmployeeTimeOffRequest request
    ) {
        findEmployee(employeeId);

        EmployeeTimeOff timeOff = 
            findEmployeeTimeOff(employeeId, timeOffId);
        
        LocalDate date =
            request.timeOffDate() != null
                ? request.timeOffDate()
                : timeOff.getTimeOffDate();
        
        Boolean fullDay =
            request.fullDay() != null
                ? request.fullDay()
                : timeOff.getFullDay();
        
        LocalTime startTime =
            request.startTime() != null
                ? request.startTime()
                : timeOff.getStartTime();
        
        LocalTime endTime =
            request.endTime() != null
                ? request.endTime()
                : timeOff.getEndTime();
        
        // Switching to a full-day absence clears the times.
        if (Boolean.TRUE.equals(fullDay)) {

            if (
                request.startTime() != null ||
                request.endTime() != null
            ) {
                throw new InvalidOperationException(
                    "Full-day time off cannot have start or end times"
                );
            }

            startTime = null;
            endTime = null;
        }

        validateTimeOff(
            date,
            fullDay,
            startTime,
            endTime
        );

        validateNoConflicts(
            employeeId,
            date,
            fullDay,
            startTime,
            endTime,
            timeOffId
        );

        timeOff.setTimeOffDate(date);
        timeOff.setFullDay(fullDay);
        timeOff.setStartTime(startTime);
        timeOff.setEndTime(endTime);

        if (request.reason() != null) {
            timeOff.setReason(
                normalizeReason(request.reason())
            );
        }

        return EmployeeTimeOffResponse.from(timeOff);
    }

    // DELETE: Remove an employee absence

    @Transactional
    public void deleteTimeOff(
        Long employeeId,
        Long timeOffId
    ) {
        findEmployee(employeeId);

        EmployeeTimeOff timeOff =
            findEmployeeTimeOff(employeeId, timeOffId);
        
        timeOffRepository.delete(timeOff);
    }

    // VALIDATION: Full-day and partial-day rules

    private void validateTimeOff(
        LocalDate date,
        Boolean fullDay,
        LocalTime startTime,
        LocalTime endTime
    ) {
        if (date == null) {
            throw new InvalidOperationException(
                "Time-off date is required"
            );
        }

        if (fullDay == null) {
            throw new InvalidOperationException(
                "Full-day status is required"
            );
        }

        if (fullDay) {
            if (startTime != null || endTime != null) {
                throw new InvalidOperationException(
                    "Full-day time off cannot have start or end times"
                );
            }

            return;
        }

        if (startTime == null || endTime == null) {
            throw new InvalidOperationException(
              "Partial-day time off requires start and end times"  
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidOperationException(
                "Time-off start must be before end time"
            );
        }
    }

    // VALIDATION: Prevent conflicting absences

    private void validateNoConflicts (
        Long employeeId,
        LocalDate date,
        Boolean fullDay,
        LocalTime startTime,
        LocalTime endTime,
        Long excludedTimeOffId
    ) {
        List<EmployeeTimeOff> existingAbsences = 
            timeOffRepository
                .findAllByEmployee_IdAndTimeOffDateOrderByStartTimeAsc(
                    employeeId,
                    date
                );
        
        for (EmployeeTimeOff existing : existingAbsences) {

            // An update should not conflict with itself.
            if (Objects.equals(
                existing.getId(),
                excludedTimeOffId
            )) {
                continue;
            }

            // Full-day absences conflict with any other
            // absence for the same employee and date.
            if (
                Boolean.TRUE.equals(fullDay) ||
                Boolean.TRUE.equals(existing.getFullDay())
            ) {
                throw new InvalidOperationException(
                    "Time off conflicts with an existing absence"
                );
            }

            // Partial-day interval overlap check.
            boolean overlaps =
                startTime.isBefore(existing.getEndTime()) &&
                endTime.isAfter(existing.getStartTime());
            
            if (overlaps) {
                throw new InvalidOperationException(
                  "Time off conflicts with an existing absence" 
                );
            }
        }
    }

    // HELPERS

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Employee not found: " + employeeId
                )
            );
    }

    private EmployeeTimeOff findEmployeeTimeOff(
        Long employeeId,
        Long timeOffId
    ) {
        EmployeeTimeOff timeOff = timeOffRepository
            .findById(timeOffId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Time off not found: " + timeOffId
                )    
            );
        
        if (!timeOff.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException(
                "Time off not found: " + timeOffId
            );
        }

        return timeOff;
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }

        return reason.trim();
    }
}
