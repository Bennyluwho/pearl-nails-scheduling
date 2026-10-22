package com.pearlnails.backend.scheduling.workinghours;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pearlnails.backend.common.exception.InvalidOperationException;
import com.pearlnails.backend.common.exception.ResourceNotFoundException;

import com.pearlnails.backend.employee.Employee;
import com.pearlnails.backend.employee.EmployeeRepository;

import com.pearlnails.backend.scheduling.businesshours.BusinessHours;
import com.pearlnails.backend.scheduling.businesshours.BusinessHoursRepository;

import com.pearlnails.backend.scheduling.workinghours.dto.CreateEmployeeWorkingHoursRequest;
import com.pearlnails.backend.scheduling.workinghours.dto.UpdateEmployeeWorkingHoursRequest;
import com.pearlnails.backend.scheduling.workinghours.dto.EmployeeWorkingHoursResponse;

@Service
public class EmployeeWorkingHoursService {

    private final EmployeeWorkingHoursRepository workingHoursRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessHoursRepository businessHoursRepository;

    public EmployeeWorkingHoursService(
        EmployeeWorkingHoursRepository workingHoursRepository,
        EmployeeRepository employeeRepository,
        BusinessHoursRepository businessHoursRepository
    ) {
        this.workingHoursRepository = workingHoursRepository;
        this.employeeRepository = employeeRepository;
        this.businessHoursRepository = businessHoursRepository;
    }

    //READ: Get all weekly shifts for an employee

    @Transactional(readOnly = true)
    public List<EmployeeWorkingHoursResponse> getWorkingHours(
        Long employeeId
    ) {
        findEmployee(employeeId);

        return workingHoursRepository
            .findAllByEmployee_IdOrderByDayOfWeekAscStartTimeAsc(
                employeeId
            )
            .stream()
            .map(EmployeeWorkingHoursResponse::from)
            .toList();
    }

    //CREATE: Add a new weekly shift

    @Transactional
    public EmployeeWorkingHoursResponse createWorkingHours(
        Long employeeId,
        CreateEmployeeWorkingHoursRequest request
    ) {
        Employee employee = findEmployee(employeeId);
        requireActiveEmployee(employee);

        validateShift(
            employeeId,
            request.dayOfWeek(),
            request.startTime(),
            request.endTime(),
            null
        );

        EmployeeWorkingHours shift = new EmployeeWorkingHours(
            employee,
            request.dayOfWeek(),
            request.startTime(),
            request.endTime()
        );

        return EmployeeWorkingHoursResponse.from(
            workingHoursRepository.save(shift)
        );
    }

    // UPDATE: Edit an existing weekly shift
    
    @Transactional
    public EmployeeWorkingHoursResponse updateWorkingHours(
        Long employeeId,
        Long shiftId,
        UpdateEmployeeWorkingHoursRequest request
    ) {
        Employee employee = findEmployee(employeeId);
        requireActiveEmployee(employee);

        EmployeeWorkingHours shift =
            findEmployeeShift(employeeId, shiftId);

        Integer dayOfWeek =
            request.dayOfWeek() != null
                ? request.dayOfWeek()
                : shift.getDayOfWeek();

        LocalTime startTime = 
            request.startTime() != null
                ? request.startTime()
                : shift.getStartTime();
        
        LocalTime endTime = 
            request.endTime() != null
                ? request.endTime()
                : shift.getEndTime();
        
        validateShift(
            employeeId,
            dayOfWeek,
            startTime,
            endTime,
            shiftId
        );

        shift.setDayOfWeek(dayOfWeek);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);

        return EmployeeWorkingHoursResponse.from(shift);
    }

    //  DELETE: Remove a weekly shift

    @Transactional
    public void deleteWorkingHours(
        Long employeeId,
        Long shiftId
    ) {
        findEmployee(employeeId);

        EmployeeWorkingHours shift = 
            findEmployeeShift(employeeId, shiftId);

        workingHoursRepository.delete(shift);
    }

    // BUSINESS RULES

     private void validateShift(
        Long employeeId,
        Integer dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Long excludedShiftId
    ) {
        if (
            dayOfWeek == null ||
            dayOfWeek < 1 ||
            dayOfWeek > 7
        ) {
            throw new InvalidOperationException(
                "Day of week must be between 1 and 7"
            );
        }

        if (startTime == null || endTime == null) {
            throw new InvalidOperationException(
                "Start time and end time are required"
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidOperationException(
                "Shift start time must be before end time"
            );
        }

        BusinessHours salonHours = businessHoursRepository
            .findByDayOfWeek(dayOfWeek)
            .orElseThrow(() ->
                new InvalidOperationException(
                    "Salon hours are not configured for this day"
                )
            );

        if (
            Boolean.TRUE.equals(salonHours.getClosed()) ||
            salonHours.getOpenTime() == null ||
            salonHours.getCloseTime() == null
        ) {
            throw new InvalidOperationException(
                "Salon is closed on this day"
            );
        }

        if (
            startTime.isBefore(salonHours.getOpenTime()) ||
            endTime.isAfter(salonHours.getCloseTime())
        ) {
            throw new InvalidOperationException(
                "Employee shift must be within salon business hours"
            );
        }

        boolean overlaps = workingHoursRepository
            .findAllByEmployee_IdAndDayOfWeekOrderByStartTimeAsc(
                employeeId,
                dayOfWeek
            )
            .stream()
            .filter(existing ->
                !Objects.equals(
                    existing.getId(),
                    excludedShiftId
                )
            )
            .anyMatch(existing ->
                existing.getStartTime().isBefore(endTime) &&
                existing.getEndTime().isAfter(startTime)
            );

        if (overlaps) {
            throw new InvalidOperationException(
                "Working hours overlap an existing shift"
            );
        }
    }

    // HELPER METHODS

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Employee not found: " + employeeId                    
                )
            );
    }

    private EmployeeWorkingHours findEmployeeShift(
        Long employeeId,
        Long shiftId
    ) {
        EmployeeWorkingHours shift = 
            workingHoursRepository.findById(shiftId)
                .orElseThrow(() -> 
                    new ResourceNotFoundException(
                        "Working hours shift not found: " + shiftId 
                    )
                );
        
        if (!shift.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException(
                "Working hours shift not found: " + shiftId
            );
        }

        return shift;
    }

    private void requireActiveEmployee(Employee employee) {
        if (!Boolean.TRUE.equals(employee.getActive())) {
            throw new InvalidOperationException(
                "Cannot schedule an inactive employee"
            );
        }
    }
}
