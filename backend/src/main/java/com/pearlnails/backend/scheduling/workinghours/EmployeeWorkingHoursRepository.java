package com.pearlnails.backend.scheduling.workinghours;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeWorkingHoursRepository
    extends JpaRepository<EmployeeWorkingHours, Long> {

    List<EmployeeWorkingHours>
        findAllByEmployee_IdOrderByDayOfWeekAscStartTimeAsc(
            Long employeeId
        );

    List<EmployeeWorkingHours>
        findAllByEmployee_IdAndDayOfWeekOrderByStartTimeAsc(
            Long employeeId,
            Integer dayOfWeek
        );

    boolean existsByEmployee_IdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(
        Long employeeId,
        Integer dayOfWeek,
        LocalTime endTime,
        LocalTime startTime
    );
}