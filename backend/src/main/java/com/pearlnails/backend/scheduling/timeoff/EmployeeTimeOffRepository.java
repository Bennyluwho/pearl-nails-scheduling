
package com.pearlnails.backend.scheduling.timeoff;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeTimeOffRepository
    extends JpaRepository<EmployeeTimeOff, Long> {

    // All absences for one employee

    List<EmployeeTimeOff>
        findAllByEmployee_IdOrderByTimeOffDateAscStartTimeAsc(
            Long employeeId
        );

    // Absences for a specific employee and date

    List<EmployeeTimeOff>
        findAllByEmployee_IdAndTimeOffDateOrderByStartTimeAsc(
            Long employeeId,
            LocalDate timeOffDate
        );

    // Check whether a full-day absence already exists

    boolean existsByEmployee_IdAndTimeOffDateAndFullDayTrue(
        Long employeeId,
        LocalDate timeOffDate
    );

    // Absences within a date range

    List<EmployeeTimeOff>
        findAllByEmployee_IdAndTimeOffDateBetweenOrderByTimeOffDateAscStartTimeAsc(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
        );
}
